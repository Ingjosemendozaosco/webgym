import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ConsultasService } from '../../services/consultas.service';
import { TrabajadorNavComponent } from '../../components/trabajador-nav/trabajador-nav.component';

@Component({
  selector: 'app-trabajador-soporte',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TrabajadorNavComponent],
  templateUrl: './trabajador-soporte.component.html',
  styleUrls: ['./trabajador-soporte.component.scss']
})
export class TrabajadorSoporteComponent {
  estados = ['ABIERTA','RESPONDIDA','CERRADA'];
  estado = '';
  page = 0;
  size = 20;
  data: any = { content: [], totalElements: 0, totalPages: 0, number: 0 };
  cargando = false;
  toast = '';

  // modal responder
  modal = false;
  sel: any = null;
  respuesta = '';

  constructor(private api: ConsultasService) {
    this.buscar();
  }

  buscar(p: number = 0) {
    this.cargando = true;
    this.page = p;
    this.api.adminList({ page: this.page, size: this.size, estado: this.estado || undefined })
      .subscribe({
        next: (res) => { this.data = res || { content: [] }; this.cargando = false; },
        error: () => { this.cargando = false; }
      });
  }

  verResponder(r: any) {
    this.sel = r;
    this.respuesta = r?.respuesta || '';
    this.modal = true;
  }

  enviarRespuesta() {
    if (!this.sel?.id) return;
    const id = Number(this.sel.id);
    const msg = this.respuesta?.trim();
    if (!msg) { alert('Escribe una respuesta'); return; }
    this.api.responder(id, msg).subscribe({
      next: (res) => {
        this.toast = 'Respuesta enviada';
        setTimeout(()=> this.toast='', 1500);
        this.modal = false;
        this.sel = null;
        this.buscar(this.page);
      }
    });
  }

  cerrarTicket(r: any) {
    if (!r?.id) return;
    if (!confirm('¿Cerrar este ticket?')) return;
    this.api.cerrar(Number(r.id)).subscribe({
      next: () => { this.toast = 'Ticket cerrado'; setTimeout(()=> this.toast='', 1200); this.buscar(this.page); }
    });
  }

  pageCount(): number { return Number(this.data?.totalPages || 0); }
  canPrev(): boolean { return this.page > 0; }
  canNext(): boolean { return this.page + 1 < this.pageCount(); }
}
