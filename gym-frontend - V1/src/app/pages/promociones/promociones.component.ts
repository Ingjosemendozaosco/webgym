import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PromocionesService } from '../../services/promociones.service';

@Component({
  selector: 'app-promociones',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './promociones.component.html',
  styleUrls: ['./promociones.component.scss']
})
export class PromocionesComponent {
  q = '';
  tipo: 'PRODUCTO' | 'RESERVA' | 'GENERAL' | '' = '' as any;
  evento = '';
  categoria = '';
  categoriasProducto: string[] = [];
  page = 0;
  size = 12;
  data: any = { content: [], totalElements: 0 };

  constructor(private promos: PromocionesService) {
    this.cargarCategorias();
    this.buscar();
  }

  cargarCategorias() {
    this.promos.categoriasProducto().subscribe(cs => this.categoriasProducto = cs || []);
  }

  buscar(page: number = 0) {
    this.page = page;
    const base = { q: this.q || undefined, tipo: (this.tipo || undefined) as any, evento: this.evento || undefined, page: this.page, size: this.size };
    if (this.categoria) {
      this.promos.porCategoria({ ...base, categoria: this.categoria }).subscribe(res => this.data = res);
    } else {
      this.promos.buscar({ ...base, soloActivas: true }).subscribe(res => this.data = res);
    }
  }

  flash24h() {
    this.promos.flash(1440, { page: 0, size: this.size }).subscribe(res => { this.data = res; this.page = 0; });
  }
}
