import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminSuscripcionesService } from '../../services/admin-suscripciones.service';
import { AdminNavComponent } from '../../components/admin-nav/admin-nav.component';

@Component({
  selector: 'app-admin-suscripciones',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AdminNavComponent],
  templateUrl: './admin-suscripciones.component.html',
  styleUrls: ['./admin-suscripciones.component.scss']
})
export class AdminSuscripcionesComponent {
  tab: 'planes' | 'membresias' = 'planes';
  planes: any[] = [];
  membresias: any[] = [];
  cargando = false;
  toast = '';

  // formularios
  modal = false;
  type: 'plan' | 'membresia' = 'plan';
  editingId: number | null = null;
  f: any = {};

  // suscriptores
  modalSubs = false;
  subsDe: { tipo: 'plan' | 'membresia'; id: number; nombre: string } | null = null;
  subs: any[] = [];
  subsCargando = false;

  constructor(private api: AdminSuscripcionesService) { this.load(); }

  setTab(t: 'planes' | 'membresias') { this.tab = t; this.load(); }

  load() {
    this.cargando = true;
    if (this.tab === 'planes') {
      this.api.listarPlanes().subscribe({ next: d => { this.planes = d || []; this.cargando = false; }, error: ()=> this.cargando=false });
    } else {
      this.api.listarMembresias().subscribe({ next: d => { this.membresias = d || []; this.cargando = false; }, error: ()=> this.cargando=false });
    }
  }

  nuevo(t: 'plan' | 'membresia') {
    this.type = t; this.editingId = null;
    this.f = t === 'plan'
      ? { nombre:'', descripcion:'', beneficio:'', descuentoPorcentaje:0, duracionDias:30, tipo:'DESCUENTO', precio:0, estado:'INACTIVO', horasMaxReserva:null, horasDiaMax:null, horasSemanaMax:null }
      : { nombre:'', duracionDias:30, precio:0, tipo:'PAGADA', trialDias:null, descripcion:'', beneficios:'', descuentoPorcentaje:0, estado:'INACTIVO' };
    this.modal = true;
  }

  editar(t: 'plan' | 'membresia', r: any) {
    this.type = t; this.editingId = Number(r?.id)||null;
    this.f = { ...r };
    this.modal = true;
  }

  guardar() {
    const body = { ...this.f };
    if (this.type === 'plan') {
      if (body.tipo === 'DESCUENTO') {
        body.horasMaxReserva = null;
        body.horasDiaMax = null;
        body.horasSemanaMax = null;
      } else if (body.tipo === 'HORAS') {
        body.descuentoPorcentaje = null;
      }
      const req = this.editingId ? this.api.actualizarPlan(this.editingId, body) : this.api.crearPlan(body);
      req.subscribe({ next: ()=> { this.toastMsg(this.editingId? 'Plan actualizado':'Plan creado'); this.modal=false; this.load(); } });
    } else {
      const req = this.editingId ? this.api.actualizarMembresia(this.editingId, body) : this.api.crearMembresia(body);
      req.subscribe({ next: ()=> { this.toastMsg(this.editingId? 'Membresía actualizada':'Membresía creada'); this.modal=false; this.load(); } });
    }
  }

  eliminar(t: 'plan' | 'membresia', r: any) {
    if (!r?.id) return; if (!confirm('¿Eliminar este registro?')) return;
    const id = Number(r.id);
    const req = t==='plan' ? this.api.eliminarPlan(id) : this.api.eliminarMembresia(id);
    req.subscribe({ next: ()=> { this.toastMsg('Eliminado'); this.load(); } });
  }

  verSuscriptores(t: 'plan' | 'membresia', r: any) {
    if (!r?.id) return;
    this.subsDe = { tipo: t, id: Number(r.id), nombre: r?.nombre || '' };
    const req = t==='plan' ? this.api.suscriptoresPlan(this.subsDe.id) : this.api.suscriptoresMembresia(this.subsDe.id);
    this.subs = [];
    this.subsCargando = true;
    this.modalSubs = true;
    req.subscribe({ 
      next: list => { this.subs = list || []; this.subsCargando = false; },
      error: () => { this.subsCargando = false; this.toastMsg('No se pudieron cargar los suscriptores'); }
    });
  }

  private toastMsg(m: string){ this.toast=m; setTimeout(()=> this.toast='', 1400); }
}
