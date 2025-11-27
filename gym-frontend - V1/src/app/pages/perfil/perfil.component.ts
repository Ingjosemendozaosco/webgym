import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PerfilService } from '../../services/perfil.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { TrabajadorService } from '../../services/trabajador.service';
import { ReservasService } from '../../services/reservas.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './perfil.component.html',
  styleUrls: ['./perfil.component.scss']
})
export class PerfilComponent {
  nombre = '';
  apellido = '';
  email = '';
  telefono = '';
  direccion = '';
  fotoUrl = '';
  recordatoriosActivos = false;
  qrToken: string | null = null;
  userId?: number;
  pedidos: any[] = [];
  pedidosPendientes: any[] = [];
  pedidosHistoricos: any[] = [];
  suscripciones: any[] = [];
  activasMembresias: any[] = [];
  activasPlanes: any[] = [];
  newPass = '';
  newPass2 = '';
  showDetalle = false;
  detalle: any = null;
  detalleTipo: 'pedido' | 'suscripcion' | null = null;
  mostrarTodosPendientes = false;
  proximaReserva: any = null;
  avisoVencimiento: any = null;
  diasRestantesVencimiento: number | null = null;
  asistencias: any[] = [];
  hoy = new Date();

  constructor(private api: PerfilService, private auth: AuthService, private router: Router, private trabajador: TrabajadorService, private reservas: ReservasService) {
    this.api.verMe().subscribe({
      next: (p) => {
        this.nombre = p?.nombre || '';
        this.apellido = p?.apellido || '';
        this.email = p?.email || '';
        this.telefono = p?.telefono || '';
        this.direccion = p?.direccion || '';
        this.fotoUrl = p?.fotoUrl || '';
        this.userId = p?.id;
        this.recordatoriosActivos = !!p?.recordatoriosActivos;
        this.qrToken = p?.qrToken || null;
      },
      error: (err) => { if (err?.status === 401) this.router.navigate(['/login']); }
    });
    this.api.pedidosMe().subscribe({ next: r => { this.pedidos = r || []; this.recalcularPedidosPendientes(); }, error: err => { if (err?.status === 401) this.router.navigate(['/login']); } });
    this.api.planesMe().subscribe({ 
      next: r => {
        this.suscripciones = (r || []).slice().sort((a: any, b: any) => {
          const da = a?.fechaInicio ? new Date(a.fechaInicio).getTime() : 0;
          const db = b?.fechaInicio ? new Date(b.fechaInicio).getTime() : 0;
          return db - da; // desc
        });
        this.recalcularActivas();
      },
      error: err => { if (err?.status === 401) this.router.navigate(['/login']); }
    });

    this.api.asistenciasMe().subscribe({
      next: (list) => {
        this.asistencias = list || [];
      },
      error: err => { if (err?.status === 401) this.router.navigate(['/login']); }
    });

    this.cargarRecordatoriosReservas();
  }

  private ahoraMs() { return Date.now(); }
  private dentroDeVigencia(s: any) {
    try {
      const ini = s?.fechaInicio ? new Date(s.fechaInicio).getTime() : undefined;
      const fin = s?.fechaFin ? new Date(s.fechaFin).getTime() : undefined;
      const now = this.ahoraMs();
      if (ini && now < ini) return false;
      if (fin && now > fin) return false;
      return true;
    } catch { return true; }
  }

  private recalcularActivas() {
    const activas = (this.suscripciones || []).filter(s => s?.estado === 'ACTIVA' && this.dentroDeVigencia(s));
    this.activasMembresias = activas.filter(s => s?.tipo === 'MEMBRESIA');
    this.activasPlanes = activas.filter(s => s?.tipo === 'PLAN');
    this.calcularAvisoVencimiento(activas);
  }

  private calcularAvisoVencimiento(activas: any[]) {
    this.avisoVencimiento = null;
    this.diasRestantesVencimiento = null;
    if (!activas || !activas.length) return;
    const ahora = this.ahoraMs();
    const diaMs = 24 * 60 * 60 * 1000;
    let mejor: any = null;
    let mejorDias = Number.POSITIVE_INFINITY;
    for (const s of activas) {
      if (!s?.fechaFin) continue;
      try {
        const finMs = new Date(s.fechaFin).getTime();
        const diffMs = finMs - ahora;
        if (diffMs <= 0) continue;
        const dias = Math.ceil(diffMs / diaMs);
        if (dias > 0 && dias <= 7 && dias < mejorDias) {
          mejorDias = dias;
          mejor = s;
        }
      } catch {
        continue;
      }
    }
    if (mejor) {
      this.avisoVencimiento = mejor;
      this.diasRestantesVencimiento = mejorDias;
    }
  }

  private claveDia(date: Date) {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  diasAsistidosSemana() {
    const hoy = this.hoy;
    const diaSemana = hoy.getDay();
    const diffLunes = diaSemana === 0 ? -6 : 1 - diaSemana;
    const inicio = new Date(hoy);
    inicio.setHours(0, 0, 0, 0);
    inicio.setDate(hoy.getDate() + diffLunes);
    const fin = new Date(hoy);
    fin.setHours(23, 59, 59, 999);
    const dias = new Set<string>();
    (this.asistencias || []).forEach(a => {
      if (!a?.fechaHora) return;
      const d = new Date(a.fechaHora);
      if (isNaN(d.getTime())) return;
      if (d < inicio || d > fin) return;
      dias.add(this.claveDia(d));
    });
    return dias.size;
  }

  diasAsistidosMes() {
    const hoy = this.hoy;
    const y = hoy.getFullYear();
    const m = hoy.getMonth();
    const dias = new Set<string>();
    (this.asistencias || []).forEach(a => {
      if (!a?.fechaHora) return;
      const d = new Date(a.fechaHora);
      if (isNaN(d.getTime())) return;
      if (d.getFullYear() !== y || d.getMonth() !== m) return;
      dias.add(this.claveDia(d));
    });
    return dias.size;
  }

  private recalcularPedidosPendientes() {
    const list = this.pedidos || [];
    const estado = (p: any) => (p?.estado || '').toString().toUpperCase();
    this.pedidosPendientes = list.filter(p => estado(p) === 'PENDIENTE');
    this.pedidosHistoricos = list.filter(p => estado(p) !== 'PENDIENTE');
  }

  private cargarRecordatoriosReservas() {
    try {
      this.reservas.mias(1).subscribe({
        next: (list) => {
          const now = new Date();
          let mejor: any = null;
          let mejorTs = Number.MAX_SAFE_INTEGER;
          (list || []).forEach((r: any) => {
            if (!r?.fecha) return;
            const start = new Date(r.fecha);
            const estado = (r?.estado || '').toString().toUpperCase();
            if (estado === 'CANCELADA' || estado === 'ANULADA') return;
            if (start.getTime() < now.getTime()) return;
            if (
              start.getFullYear() === now.getFullYear() &&
              start.getMonth() === now.getMonth() &&
              start.getDate() === now.getDate()
            ) {
              const ts = start.getTime();
              if (ts < mejorTs) {
                mejorTs = ts;
                mejor = r;
              }
            }
          });
          this.proximaReserva = mejor;
        },
        error: () => {
          this.proximaReserva = null;
        }
      });
    } catch {
      this.proximaReserva = null;
    }
  }

  pedidosPendientesVisibles() {
    const list = this.pedidosPendientes || [];
    if (this.mostrarTodosPendientes) return list;
    return list.slice(0, 4);
  }

  estadoLabel(e: string) {
    switch (e) {
      case 'PENDIENTE_PAGO': return 'Pendiente de pago';
      case 'ACTIVA': return 'Activa';
      case 'RECHAZADA': return 'Rechazada';
      case 'CANCELADA': return 'Cancelada';
      case 'EXPIRADA': return 'Expirada';
      default: return e || '—';
    }
  }

  estadoClase(e: string) {
    switch (e) {
      case 'ACTIVA': return ['badge','ok'];
      case 'PENDIENTE_PAGO': return ['badge','warn'];
      case 'RECHAZADA': return ['badge','danger'];
      case 'CANCELADA':
      case 'EXPIRADA': return ['badge','off'];
      default: return ['badge'];
    }
  }

  cancelarSuscripcion(s: any) {
    if (!s?.id) return;
    if (!confirm('¿Cancelar esta suscripción?')) return;
    this.trabajador.cancelarSuscripcion(Number(s.id)).subscribe({
      next: () => {
        const it = (this.suscripciones || []).find(x => x.id === s.id);
        if (it) it.estado = 'CANCELADA';
        this.recalcularActivas();
      },
      error: (err) => { if (err?.status === 401) this.router.navigate(['/login']); }
    });
  }

  cancelarPedido(p: any) {
    if (!p?.id) return;
    if (!confirm('¿Cancelar este pedido pendiente?')) return;
    this.trabajador.cancelarPedido(Number(p.id)).subscribe({
      next: () => {
        const it = (this.pedidos || []).find(x => x.id === p.id);
        if (it) it.estado = 'CANCELADO';
        this.recalcularPedidosPendientes();
      },
      error: (err) => { if (err?.status === 401) this.router.navigate(['/login']); }
    });
  }

  eliminarSuscripcion(s: any) {
    if (!s?.id) return;
    if (!confirm('¿Eliminar esta suscripción? Esta acción no se puede deshacer.')) return;
    this.trabajador.eliminarSuscripcion(Number(s.id)).subscribe({
      next: () => {
        this.suscripciones = (this.suscripciones || []).filter(x => x.id !== s.id);
        this.activasMembresias = (this.activasMembresias || []).filter(x => x.id !== s.id);
        this.activasPlanes = (this.activasPlanes || []).filter(x => x.id !== s.id);
        if (this.showDetalle) this.cerrarDetalle();
      },
      error: (err) => {
        if (err?.status === 401) this.router.navigate(['/login']);
        const msg = err?.error?.message || err?.statusText || 'No se pudo eliminar la suscripción';
        alert(msg);
      }
    });
  }

  abrirDetallePedido(p: any) {
    this.detalleTipo = 'pedido';
    this.detalle = p;
    this.showDetalle = true;
  }

  abrirDetalleSuscripcion(s: any) {
    this.detalleTipo = 'suscripcion';
    this.detalle = s;
    this.showDetalle = true;
  }

  cerrarDetalle() {
    this.showDetalle = false;
    this.detalle = null;
    this.detalleTipo = null;
  }

  verHistorialPagos() {
	    this.router.navigate(['/historial/pagos']);
  }

  verHistorialSuscripciones() {
	    this.router.navigate(['/historial/suscripciones']);
  }

  verComprasConfirmadas() {
	    this.router.navigate(['/compras-confirmadas']);
  }

  get qrImageUrl(): string | null {
    if (!this.qrToken) return null;
    const data = encodeURIComponent(this.qrToken);
    return `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${data}`;
  }

  asistenciasVisibles(max: number = 10) {
    const list = this.asistencias || [];
    return list.slice(0, max);
  }

  guardar() {
    const dto = {
      nombre: this.nombre,
      apellido: this.apellido,
      telefono: this.telefono,
      direccion: this.direccion,
      fotoUrl: this.fotoUrl,
      recordatoriosActivos: this.recordatoriosActivos
    };
    this.api.editarMe(dto).subscribe({ next: () => alert('Perfil actualizado'), error: (err) => { if (err?.status === 401) this.router.navigate(['/login']); } });
  }

  cambiarPassword() {
    if (!this.newPass || this.newPass !== this.newPass2) { alert('Las contraseñas no coinciden'); return; }
    this.api.passwordMe(this.newPass).subscribe({
      next: () => { this.newPass = this.newPass2 = ''; alert('Contraseña actualizada'); },
      error: (err) => { if (err?.status === 401) this.router.navigate(['/login']); }
    });
  }
}
