import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminPromocionesService } from '../../services/admin-promociones.service';
import { AdminNavComponent } from '../../components/admin-nav/admin-nav.component';
import { AdminProductosService } from '../../services/admin-productos.service';

@Component({
  selector: 'app-admin-promociones',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AdminNavComponent],
  templateUrl: './admin-promociones.component.html',
  styleUrls: ['./admin-promociones.component.scss']
})
export class AdminPromocionesComponent {
  // list
  soloActivas = true;
  data: any[] = [];
  cargando = false;
  toast = '';

  // form
  modal = false;
  editingId: number | null = null;
  f: any = { titulo:'', descripcion:'', tipo:'GENERAL', descuentoPorcentaje:0, activo:true, fechaInicio:'', fechaFin:'', evento:'', categoria:'' };
  // producto selector
  prodQuery = '';
  prodResults: any[] = [];
  selectedProducts: any[] = [];

  constructor(private api: AdminPromocionesService, private productosApi: AdminProductosService) { this.buscar(); }

  buscar() {
    this.cargando = true;
    this.api.listar(this.soloActivas).subscribe({ 
      next: res => { this.data = (res?.content) || []; this.cargando = false; }, 
      error: (err)=> { 
        this.cargando=false; 
        this.toastMsg(err?.error?.message || 'Error cargando promociones. ¿Reiniciaste el backend?');
      } 
    });
  }

  nuevo() { 
    this.editingId=null; 
    this.f = { titulo:'', descripcion:'', tipo:'GENERAL', descuentoPorcentaje:0, activo:true, fechaInicio:'', fechaFin:'', evento:'', categoria:'' };
    this.selectedProducts = [];
    this.prodQuery=''; this.prodResults=[];
    this.modal = true; 
  }
  editar(p: any) {
    this.editingId = Number(p?.id) || null;
    this.f = { titulo:p.titulo||'', descripcion:p.descripcion||'', tipo:p.tipo||'GENERAL', descuentoPorcentaje:p.descuentoPorcentaje||0, activo: !!p.activo,
      fechaInicio: p.fechaInicio ? p.fechaInicio.substring(0,16) : '', fechaFin: p.fechaFin ? p.fechaFin.substring(0,16) : '', evento: p.evento||'', categoria: p.categoria||'' };
    this.selectedProducts = (p.productos || []).slice();
    this.prodQuery=''; this.prodResults=[];
    this.modal = true;
  }

  guardar() {
    const productoIds = (this.selectedProducts || []).map((x:any)=> Number(x.id)).filter((n:number)=>!isNaN(n));
    const body = { titulo: this.f.titulo, descripcion: this.f.descripcion, tipo: this.f.tipo, descuentoPorcentaje: Number(this.f.descuentoPorcentaje||0), activo: !!this.f.activo,
      fechaInicio: this.f.fechaInicio ? new Date(this.f.fechaInicio).toISOString() : null,
      fechaFin: this.f.fechaFin ? new Date(this.f.fechaFin).toISOString() : null,
      evento: (this.f.evento||'').trim() || null,
      categoria: (this.f.categoria||'').trim() || null,
      productoIds };
    const req = this.editingId ? this.api.actualizar(this.editingId, body) : this.api.crear(body);
    req.subscribe({ 
      next: () => { this.toastMsg(this.editingId? 'Promoción actualizada':'Promoción creada'); this.modal=false; this.buscar(); }, 
      error:(err)=>{ 
        const msg = err?.status === 401 || err?.status === 403 ? 'No autorizado. Inicia sesión como ADMIN.' : 'No se pudo guardar la promoción.';
        this.toastMsg(msg);
      } 
    });
  }

  eliminar(p: any) { 
    if (!p?.id) return; 
    if (!confirm('¿Eliminar esta promoción?')) return; 
    this.api.eliminar(Number(p.id)).subscribe({ 
      next:()=>{ this.toastMsg('Promoción eliminada'); this.buscar(); }, 
      error:(err)=>{ 
        const msg = err?.status === 401 || err?.status === 403 ? 'No autorizado para eliminar.' : 'No se pudo eliminar.';
        this.toastMsg(msg);
      }
    }); 
  }

  buscarProductos() {
    const q = (this.prodQuery||'').trim();
    if (!q) { this.prodResults = []; return; }
    this.productosApi.buscar({ q, categoria: undefined, soloConStock: false, mostrarTodos: true, disponible: undefined, page: 0, size: 8 })
      .subscribe({ next: (res:any) => { this.prodResults = res?.content || []; } });
  }

  toggleProducto(p:any) {
    const idx = this.selectedProducts.findIndex(x=> x.id === p.id);
    if (idx>=0) this.selectedProducts.splice(idx,1);
    else this.selectedProducts.push(p);
  }

  isSelected(p:any): boolean {
    return Array.isArray(this.selectedProducts) && this.selectedProducts.some(x => x?.id === p?.id);
  }

  private toastMsg(m: string){ this.toast=m; setTimeout(()=> this.toast='', 1400); }
}
