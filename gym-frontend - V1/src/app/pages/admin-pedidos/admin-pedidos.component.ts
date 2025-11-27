import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminPedidosService } from '../../services/admin-pedidos.service';
import { AdminNavComponent } from '../../components/admin-nav/admin-nav.component';

@Component({
  selector: 'app-admin-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AdminNavComponent],
  templateUrl: './admin-pedidos.component.html',
  styleUrls: ['./admin-pedidos.component.scss']
})
export class AdminPedidosComponent {
  estados: string[] = ['RECHAZADO','REVISION'];
  selected: Set<string> = new Set(this.estados);
  page = 0; size = 20;
  data: any = { content: [], totalElements: 0, totalPages: 0, number: 0 };
  cargando = false;
  toast = '';

  constructor(private api: AdminPedidosService) { this.buscar(); }

  toggleEstado(e: string) {
    if (this.selected.has(e)) this.selected.delete(e); else this.selected.add(e);
    this.buscar(0);
  }

  buscar(p: number = 0) {
    this.cargando = true; this.page = p;
    const estados = Array.from(this.selected);
    this.api.listar({ page: this.page, size: this.size, estados }).subscribe({
      next: res => { this.data = res || { content: [] }; this.cargando = false; },
      error: () => { this.cargando = false; }
    });
  }

  anular(ped: any) {
    if (!ped?.id) return;
    if (!confirm('¿Anular este pedido?')) return;
    this.api.anular(Number(ped.id)).subscribe({ next: () => { this.toast='Pedido anulado'; setTimeout(()=>this.toast='',1200); this.buscar(this.page); } });
  }

  pageCount(): number { return Number(this.data?.totalPages || 0); }
  canPrev(): boolean { return this.page > 0; }
  canNext(): boolean { return this.page + 1 < this.pageCount(); }
}
