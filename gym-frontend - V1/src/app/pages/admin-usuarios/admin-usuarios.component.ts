import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminUsuariosService } from '../../services/admin-usuarios.service';
import { AdminNavComponent } from '../../components/admin-nav/admin-nav.component';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AdminNavComponent],
  templateUrl: './admin-usuarios.component.html',
  styleUrls: ['./admin-usuarios.component.scss']
})
export class AdminUsuariosComponent {
  q = '';
  rol = '';
  activo: string = '';
  page = 0;
  size = 20;
  data: any = { content: [], totalElements: 0, totalPages: 0, number: 0 };
  cargando = false;
  toast = '';

  roles = ['USUARIO','TRABAJADOR','ADMIN']; // para mostrar el rol actual
  selectionRoles = ['USUARIO','TRABAJADOR']; // solo estos pueden seleccionarse

  constructor(private api: AdminUsuariosService) { this.buscar(); }

  buscar(p: number = 0) {
    this.cargando = true;
    this.page = p;
    const activoVal = this.activo === '' ? null : this.activo === 'true';
    this.api.listar({ page: this.page, size: this.size, q: this.q?.trim() || undefined, rol: this.rol || undefined, activo: activoVal })
      .subscribe({ next: res => {
        // Normalizar activo -> activoCode num (1 activo, 0 bloqueado)
        if (res && Array.isArray(res.content)) {
          res.content = res.content.map((u: any) => ({
            ...u,
            activoCode: (u?.activo === false || u?.activo === 0) ? 0 : 1
          }));
        }
        this.data = res || { content: [] };
        this.cargando = false;
      }, error: () => { this.cargando = false; } });
  }

  resetFilters() { this.q=''; this.rol=''; this.activo=''; this.buscar(0); }

  setRol(u: any, r: string) {
    if (!u?.id) return;
    // No permitir establecer ADMIN desde aquí; tampoco cambiar usuarios con rol ADMIN
    if (r === 'ADMIN' || u.rol === 'ADMIN') { this.toastMsg('El rol ADMIN no se puede modificar aquí'); return; }
    if (!this.selectionRoles.includes(r)) { this.toastMsg('Rol inválido'); return; }
    this.api.setRol(Number(u.id), r).subscribe({ next: () => { u.rol = r; this.toastMsg('Rol actualizado'); } });
  }

  toggleActivo(u: any) {
    if (!u?.id) return;
    // Usar activoCode local (1 activo, 0 bloqueado). Backend espera boolean.
    const estaBloqueado = (u.activoCode === 0);
    const val = estaBloqueado ? true : false; // true=activar, false=bloquear
    this.api.setActivo(Number(u.id), val).subscribe({ 
      next: () => { 
        // Sincronizar: activo boolean para backend y activoCode para UI
        u.activo = val; // boolean
        u.activoCode = val ? 1 : 0; 
        this.toastMsg(val ? 'Usuario activado' : 'Usuario bloqueado'); 
      } 
    });
  }

  pageCount(): number { return Number(this.data?.totalPages || 0); }
  canPrev(): boolean { return this.page > 0; }
  canNext(): boolean { return this.page + 1 < this.pageCount(); }

  private toastMsg(msg: string) { this.toast = msg; setTimeout(()=> this.toast='', 1400); }

  // Helpers para plantilla (fallback a boolean si no hay code)
  isActivo(u: any): boolean { return (u?.activoCode ?? (u?.activo === false ? 0 : 1)) === 1; }
  estadoLabel(u: any): string { return this.isActivo(u) ? 'ACTIVO' : 'BLOQUEADO'; }
  toggleLabel(u: any): string { return this.isActivo(u) ? 'Bloquear' : 'Activar'; }
}
