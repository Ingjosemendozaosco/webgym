import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TrabajadorService } from '../../services/trabajador.service';
import { TrabajadorNavComponent } from '../../components/trabajador-nav/trabajador-nav.component';

@Component({
  selector: 'app-trabajador-planes',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TrabajadorNavComponent],
  templateUrl: './trabajador-planes.component.html',
  styleUrls: ['./trabajador-planes.component.scss']
})
export class TrabajadorPlanesComponent implements OnInit {
  planes: any[] = [];
  filtroEstado: string = 'PENDIENTE_PAGO';
  cargando = false;
  error: string | null = null;

  constructor(private ws: TrabajadorService) {}

  ngOnInit(): void { this.cargar(); }

  cargar() {
    this.cargando = true;
    this.error = null;
    const params: any = { tipo: 'PLAN' };
    if (this.filtroEstado) params.estado = this.filtroEstado;
    this.ws.listarSuscripciones(params).subscribe({
      next: (list) => { this.planes = list || []; this.cargando = false; },
      error: () => { this.error = 'No se pudo cargar'; this.cargando = false; }
    });
  }

  F(s: string) { const d = new Date(s); return isNaN(d.getTime()) ? '—' : d.toLocaleString('es-PE', { hour12: false }); }

  activar(id: number) {
    if (this.cargando) return;
    this.cargando = true;
    this.ws.confirmarSuscripcion(id).subscribe({
      next: () => { this.cargar(); },
      error: () => { this.error = 'No se pudo activar'; this.cargando = false; }
    });
  }

  rechazar(id: number) {
    if (this.cargando) return;
    const motivo = window.prompt('Motivo de rechazo (opcional):') || undefined;
    this.cargando = true;
    this.ws.rechazarSuscripcion(id, motivo).subscribe({
      next: () => { this.cargar(); },
      error: () => { this.error = 'No se pudo rechazar'; this.cargando = false; }
    });
  }

  estadoClase(estado: string) {
    switch (estado) {
      case 'ACTIVA': return 'is-activa';
      case 'RECHAZADA': return 'is-rechazada';
      case 'PENDIENTE_PAGO': return 'is-pendiente';
      case 'CANCELADA': return 'is-cancelada';
      case 'EXPIRADA': return 'is-expirada';
      default: return 'is-muted';
    }
  }

  cancelar(id: number) {
    if (this.cargando) return;
    const motivo = window.prompt('Motivo de cancelación (opcional):') || undefined;
    this.cargando = true;
    this.ws.cancelarSuscripcion(id, motivo).subscribe({
      next: () => { this.cargar(); },
      error: () => { this.error = 'No se pudo cancelar'; this.cargando = false; }
    });
  }

  // Opciones de estado con etiquetas legibles
  estadoOpciones = [
    { value: '', label: 'Todos' },
    { value: 'PENDIENTE_PAGO', label: 'Pendiente de pago' },
    { value: 'ACTIVA', label: 'Activa' },
    { value: 'RECHAZADA', label: 'Rechazada' },
    { value: 'CANCELADA', label: 'Cancelada' },
    { value: 'EXPIRADA', label: 'Expirada' }
  ];

  estadoLabel(estado: string): string {
    switch (estado) {
      case 'PENDIENTE_PAGO': return 'Pendiente de pago';
      case 'ACTIVA': return 'Activa';
      case 'RECHAZADA': return 'Rechazada';
      case 'CANCELADA': return 'Cancelada';
      case 'EXPIRADA': return 'Expirada';
      default: return estado || '—';
    }
  }
}
