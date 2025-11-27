import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TrabajadorService } from '../../services/trabajador.service';

@Component({
  selector: 'app-trabajador',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './trabajador.component.html',
  styleUrls: ['./trabajador.component.scss']
})
export class TrabajadorComponent implements OnInit {
  // Estado UI
  cargando = false;
  error: string | null = null;

  // Datos
  pedidos: any[] = [];
  filtroEstado: string = '';
  filtroEstadoPago: string = '';
  pagos: any[] = [];
  filtroPagoEstado: string = '';
  membresias: any[] = [];
  filtroMembresiaEstado: string = '';
  planes: any[] = [];
  filtroPlanEstado: string = '';
  reservacionesPendientes: Array<any> = [
    { id: 501, estado: 'PENDIENTE', fechaHora: new Date().toISOString(), producto: { nombre: 'Mancuernas 10kg' } },
    { id: 502, estado: 'PENDIENTE', fechaHora: new Date().toISOString(), membresia: { nombre: 'Plan Mensual' } },
  ];

  constructor(public auth: AuthService, private router: Router, private ws: TrabajadorService) {}

  ngOnInit(): void {
    this.cargarPedidos();
    this.cargarPagos();
    this.cargarMembresias();
    this.cargarPlanes();
  }

  cargarPedidos() {
    this.cargando = true;
    this.error = null;
    const params: any = {};
    if (this.filtroEstado) params.estado = this.filtroEstado;
    if (this.filtroEstadoPago) params.estadoPago = this.filtroEstadoPago;
    this.ws.listarPedidos(params).subscribe({
      next: (list) => {
        const all = (list || []).slice();
        all.sort((a: any, b: any) => {
          const da = a?.fechaPedido ? new Date(a.fechaPedido).getTime() : 0;
          const db = b?.fechaPedido ? new Date(b.fechaPedido).getTime() : 0;
          return db - da;
        });
        this.pedidos = all.slice(0, 10);
        this.cargando = false;
      },
      error: () => { this.error = 'No se pudo cargar pedidos'; this.cargando = false; }
    });
  }

  confirmar(p: any) {
    if (!confirm(`Confirmar pedido #${p.id}?`)) return;
    this.ws.confirmarPedido(p.id).subscribe({
      next: () => this.cargarPedidos(),
      error: () => alert('No se pudo confirmar')
    });
  }

  cancelar(p: any) {
    if (!confirm(`Cancelar pedido #${p.id}?`)) return;
    this.ws.cancelarPedido(p.id).subscribe({
      next: () => this.cargarPedidos(),
      error: () => alert('No se pudo cancelar')
    });
  }

  cargarPagos() {
    const params: any = {};
    if (this.filtroPagoEstado) params.estado = this.filtroPagoEstado;
    this.ws.listarPagos(params).subscribe({
      next: (list) => { this.pagos = list || []; },
      error: () => {}
    });
  }

  cargarMembresias() {
    const params: any = { tipo: 'MEMBRESIA' };
    if (this.filtroMembresiaEstado) params.estado = this.filtroMembresiaEstado;
    this.ws.listarSuscripciones(params).subscribe({
      next: (list) => { this.membresias = list || []; },
      error: () => {}
    });
  }

  cargarPlanes() {
    const params: any = { tipo: 'PLAN' };
    if (this.filtroPlanEstado) params.estado = this.filtroPlanEstado;
    this.ws.listarSuscripciones(params).subscribe({
      next: (list) => { this.planes = list || []; },
      error: () => {}
    });
  }

  // Computados: pendientes de aprobación
  get membresiasPendientes(): any[] {
    return (this.membresias || []).filter(s => this.esPendienteAprobacion(s?.estado));
  }
  get planesPendientes(): any[] {
    return (this.planes || []).filter(s => this.esPendienteAprobacion(s?.estado));
  }

  private esPendienteAprobacion(estado?: string): boolean {
    if (!estado) return false;
    const e = estado.toString().toUpperCase();
    // Contemplar variantes comunes de backend
    return e.includes('PENDIENTE') || e.includes('APROB') || e.includes('SOLIC');
  }

  cerrarSesion() {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }

  formatearMoneda(v: number): string {
    return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(v || 0);
  }

  formatearFecha(fecha: string): string {
    const d = new Date(fecha);
    if (isNaN(d.getTime())) return '—';
    return d.toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  get gananciaHoy(): number {
    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm = hoy.getMonth();
    const dd = hoy.getDate();
    const start = new Date(yyyy, mm, dd, 0, 0, 0, 0).getTime();
    const end = new Date(yyyy, mm, dd, 23, 59, 59, 999).getTime();
    const pagos = this.pagos || [];
    const isHoy = (f?: string) => {
      if (!f) return false;
      const t = new Date(f).getTime();
      return t >= start && t <= end;
    };
    return pagos
      .filter((p: any) => (p?.estado || '').toUpperCase() === 'CONFIRMADO')
      .filter((p: any) => isHoy(p?.fechaConfirmacion) || isHoy(p?.fechaPago) || isHoy(p?.fecha))
      .reduce((acc: number, p: any) => acc + Number(p?.monto || p?.total || 0), 0);
  }

  estadoPedidoClase(estado?: string) {
    switch (estado) {
      case 'CONFIRMADO': return ['badge', 'badge-success'];
      case 'CANCELADO': return ['badge', 'badge-muted'];
      case 'RECHAZADO': return ['badge', 'badge-danger'];
      case 'PENDIENTE': return ['badge', 'badge-warning'];
      default: return ['badge', 'badge-muted'];
    }
  }

  estadoPagoClase(estado?: string) {
    switch (estado) {
      case 'CONFIRMADO': return ['badge', 'badge-success'];
      case 'CANCELADO': return ['badge', 'badge-muted'];
      case 'RECHAZADO': return ['badge', 'badge-danger'];
      case 'PENDIENTE': return ['badge', 'badge-info'];
      default: return ['badge', 'badge-muted'];
    }
  }

  eliminar(p: any) {
    if (p?.estado !== 'CANCELADO') { alert('Solo puedes eliminar pedidos CANCELADOS'); return; }
    if (!confirm(`Eliminar pedido #${p.id}? Esta acción es permanente.`)) return;
    this.ws.eliminarPedido(p.id).subscribe({
      next: () => this.cargarPedidos(),
      error: () => alert('No se pudo eliminar')
    });
  }

  // Modal de detalle de pedido
  detalleAbierto: boolean = false;
  pedidoSeleccionado: any | null = null;

  verMas(p: any) {
    this.pedidoSeleccionado = p;
    this.detalleAbierto = true;
  }

  cerrarDetalle() {
    this.detalleAbierto = false;
    this.pedidoSeleccionado = null;
  }
}
