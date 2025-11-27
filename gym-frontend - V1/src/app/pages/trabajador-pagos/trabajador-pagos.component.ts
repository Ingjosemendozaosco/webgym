import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TrabajadorService } from '../../services/trabajador.service';

@Component({
  selector: 'app-trabajador-pagos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './trabajador-pagos.component.html'
})
export class TrabajadorPagosComponent implements OnInit {
  pagos: any[] = [];
  filtroEstado = '';
  cargando = false;
  error: string | null = null;

  constructor(private ws: TrabajadorService) {}

  ngOnInit(): void { this.cargar(); }

  cargar() {
    this.cargando = true;
    this.error = null;
    const params: any = {};
    if (this.filtroEstado) params.estado = this.filtroEstado;
    this.ws.listarPagos(params).subscribe({
      next: (list) => { this.pagos = list || []; this.cargando = false; },
      error: () => { this.error = 'No se pudo cargar'; this.cargando = false; }
    });
  }

  P(v: number) { return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(v || 0); }
  F(s: string) { const d = new Date(s); return isNaN(d.getTime()) ? '—' : d.toLocaleString('es-PE', { hour12: false }); }
}
