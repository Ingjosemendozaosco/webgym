import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminProductosService } from '../../services/admin-productos.service';
import { AdminNavComponent } from '../../components/admin-nav/admin-nav.component';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AdminNavComponent],
  templateUrl: './admin-productos.component.html',
  styleUrls: ['./admin-productos.component.scss']
})
export class AdminProductosComponent {
  // list
  q = '';
  categoria = '';
  soloConStock = false;
  mostrarTodos = false; // incluir no disponibles
  filtroDisponible: '' | 'true' | 'false' = '';
  disponible: boolean | undefined = undefined;
  categorias: string[] = [];
  page = 0; size = 12;
  data: any = { content: [], totalElements: 0, totalPages: 0, number: 0 };
  cargando = false;
  toast = '';

  // form
  modal = false;
  editingId: number | null = null;
  f: any = { nombre: '', descripcion: '', precio: 0, stock: 0, categoria: '', descuento: 0, disponible: true, imagen: '' };

  constructor(private api: AdminProductosService) { this.init(); }

  init() {
    this.api.categorias().subscribe(c => this.categorias = c || []);
    this.buscar(0);
  }

  buscar(p: number = 0) {
    this.cargando = true; this.page = p;
    // Mapear filtroDisponible a disponible boolean | undefined
    this.disponible = this.filtroDisponible === '' ? undefined : this.filtroDisponible === 'true';
    this.api.buscar({ q: this.q?.trim() || '', categoria: this.categoria || undefined, soloConStock: this.soloConStock, mostrarTodos: this.mostrarTodos, disponible: this.disponible, page: this.page, size: this.size })
      .subscribe({ next: res => { this.data = res || { content: [] }; this.cargando = false; }, error: () => { this.cargando = false; } });
  }
  resetFilters() { this.q=''; this.categoria=''; this.soloConStock=false; this.mostrarTodos=false; this.filtroDisponible=''; this.disponible=undefined; this.buscar(0); }

  nuevo() { this.editingId = null; this.f = { nombre:'', descripcion:'', precio:0, stock:0, categoria:'', descuento:0, disponible:true, imagen:'' }; this.modal = true; }
  editar(p: any) { this.editingId = Number(p?.id) || null; this.f = { nombre:p.nombre||'', descripcion:p.descripcion||'', precio:p.precio||0, stock:p.stock||0, categoria:p.categoria||'', descuento:p.descuento||0, disponible: !!p.disponible, imagen: p.imagen || '' }; this.modal = true; }

  guardar() {
    const body = { ...this.f, precio: Number(this.f.precio||0), stock: Number(this.f.stock||0), descuento: Number(this.f.descuento||0), imagen: (this.f.imagen||'').trim() };
    const req = this.editingId ? this.api.actualizar(this.editingId, body) : this.api.crear(body);
    req.subscribe({ next: () => { this.toastMsg(this.editingId? 'Producto actualizado':'Producto creado'); this.modal=false; this.buscar(this.page); }, error:()=>{} });
  }

  eliminar(p: any) {
    if (!p?.id) return; if (!confirm('¿Eliminar este producto?')) return;
    this.api.eliminar(Number(p.id)).subscribe({ next:()=>{ this.toastMsg('Producto eliminado'); this.buscar(this.page); } });
  }

  pageCount(): number { return Number(this.data?.totalPages || 0); }
  canPrev(): boolean { return this.page > 0; }
  canNext(): boolean { return this.page + 1 < this.pageCount(); }
  private toastMsg(m: string){ this.toast=m; setTimeout(()=> this.toast='', 1400); }
}
