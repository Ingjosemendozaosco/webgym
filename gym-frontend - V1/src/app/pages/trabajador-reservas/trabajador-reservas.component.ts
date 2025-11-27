import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReservasService } from '../../services/reservas.service';
import { TrabajadorNavComponent } from '../../components/trabajador-nav/trabajador-nav.component';

@Component({
  selector: 'app-trabajador-reservas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TrabajadorNavComponent],
  templateUrl: './trabajador-reservas.component.html',
  styleUrls: ['./trabajador-reservas.component.scss']
})
export class TrabajadorReservasComponent {
  page = 0;
  size = 20;
  estado = '';
  data: any = { content: [], totalElements: 0, totalPages: 0, number: 0 };
  cargando = false;
  toast = '';

  estados = ['PENDIENTE','CONFIRMADA','CANCELADA','REVISION','ANULADA'];

  constructor(private api: ReservasService) {
    this.buscar();
  }

  buscar(p: number = 0) {
    this.cargando = true;
    this.page = p;
    this.api.adminList({ page: this.page, size: this.size, estado: this.estado || undefined })
      .subscribe({
        next: res => {
          // inicializar _nuevoEstado en cada fila para evitar enviar undefined
          const content = (res?.content || []).map((r: any) => ({ ...r, _nuevoEstado: r?.estado }));
          this.data = { ...res, content };
          this.cargando = false;
        },
        error: () => { this.cargando = false; }
      });
  }

  cambiarEstado(r: any, nuevo: string) {
    if (!r?.id) return;
    const target = nuevo || r._nuevoEstado || r.estado;
    this.api.cambiarEstado(Number(r.id), target).subscribe({
      next: () => {
        // Actualiza en UI al instante
        r.estado = target;
        r._nuevoEstado = target;
        this.toast = `#${r.id} → ${target}`;
        setTimeout(()=> this.toast='', 1500);
      },
      error: () => {
        alert('No se pudo actualizar el estado');
      }
    });
  }

  estadoClase(estado: string) {
    switch (estado) {
      case 'CONFIRMADA': return 'is-confirmada';
      case 'PENDIENTE': return 'is-pendiente';
      case 'REVISION': return 'is-revision';
      case 'ANULADA': return 'is-anulada';
      case 'CANCELADA': return 'is-cancelada';
      default: return 'is-muted';
    }
  }

  pageCount(): number { return Number(this.data?.totalPages || 0); }
  canPrev(): boolean { return this.page > 0; }
  canNext(): boolean { return this.page + 1 < this.pageCount(); }
}
