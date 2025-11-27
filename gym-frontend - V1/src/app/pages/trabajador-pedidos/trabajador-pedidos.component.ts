import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TrabajadorService } from '../../services/trabajador.service';
import { TrabajadorNavComponent } from '../../components/trabajador-nav/trabajador-nav.component';

@Component({
  selector: 'app-trabajador-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TrabajadorNavComponent],
  templateUrl: './trabajador-pedidos.component.html',
  styleUrls: ['./trabajador-pedidos.component.scss']
})
export class TrabajadorPedidosComponent implements OnInit {
  pedidos: any[] = [];
  filtroEstado = '';
  filtroEstadoPago = '';
  cargando = false;
  error: string | null = null;

  constructor(private ws: TrabajadorService) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
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
        this.pedidos = all;
        this.cargando = false;
      },
      error: () => { this.error = 'No se pudo cargar'; this.cargando = false; }
    });
  }

  confirmar(p: any) {
    if (!confirm(`Confirmar pedido #${p.id}?`)) return;
    this.ws.confirmarPedido(p.id).subscribe({ next: () => this.cargar() });
  }

  cancelar(p: any) {
    if (!confirm(`Cancelar pedido #${p.id}?`)) return;
    this.ws.cancelarPedido(p.id).subscribe({ next: () => this.cargar() });
  }

  rechazar(p: any) {
    if (!p?.id) return;
    const motivo = window.prompt('Motivo de rechazo (opcional):') || undefined;
    if (!confirm(`Rechazar pedido #${p.id}?`)) return;
    this.ws.rechazarPedido(p.id, motivo).subscribe({ next: () => this.cargar() });
  }

  revisar(p: any) {
    if (!p?.id) return;
    const nota = window.prompt('Nota para revisión (opcional):') || undefined;
    if (!confirm(`Marcar en revisión pedido #${p.id}?`)) return;
    this.ws.marcarRevisionPedido(p.id, nota).subscribe({ next: () => this.cargar() });
  }

  P(v: number) { return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(v || 0); }
  F(s: string) {
    const d = new Date(s); if (isNaN(d.getTime())) return '—';
    return d.toLocaleString('es-PE', { hour12: false });
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
    this.ws.eliminarPedido(p.id).subscribe({ next: () => this.cargar() });
  }

  exportarPdf(p: any) {
    if (!p?.id) return;
    this.ws.descargarComprobantePdf(p.id).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `comprobante-pedido-${p.id}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  // Modal de detalle de pedido
  detalleAbierto = false;
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
