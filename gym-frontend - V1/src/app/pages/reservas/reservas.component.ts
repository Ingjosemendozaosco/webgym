import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ReservasService } from '../../services/reservas.service';
import { PerfilService } from '../../services/perfil.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reservas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reservas.component.html',
  styleUrls: ['./reservas.component.scss']
})
export class ReservasComponent {
  q = '';
  categoria = '';
  soloConStock = true;
  usos: string[] = [];
  page = 0;
  size = 12;
  data: any = { content: [], totalElements: 0 };
  miasList: any[] = [];

  // formulario de reserva simple
  fecha = '';
  duracionMinutos = 60;
  maxDurMin = 60; // límite dinámico según plan HORAS (60 * horasMaxReserva)
  productoIdSeleccionado: number | null = null;
  selectedProducto: any = null;
  toastMsg = '';
  showToast = false;
  activeTab: 'explorar' | 'mias' = 'explorar';
  showModal = false;
  modalMode: 'agenda' | 'reserva' = 'reserva';

  // Agenda semanal
  weekStart!: Date; // lunes de la semana actual
  days: { date: Date; label: string; iso: string }[] = [];
  hours: number[] = Array.from({ length: 16 }, (_, i) => 6 + i); // 06..21
  ocupados = new Set<string>(); // keys: yyyy-MM-ddTHH:00
  reservasByKey = new Map<string, any>();
  statusByKey = new Map<string, string>();

  constructor(private reservas: ReservasService, private auth: AuthService, private perfil: PerfilService) {
    this.cargarUsos();
    this.buscar();
    this.cargarMias();
    this.initWeek();
    this.cargarCapacidad();
  }

  estadoKey(dayIso: string, hour: number): string | null {
    const key = this.slotKey(dayIso, hour);
    return this.statusByKey.get(key) || null;
  }

  eliminar(r: any) {
    if (!r?.id) return;
    if ((r?.estado || '').toString().toUpperCase() !== 'CANCELADA') { alert('Solo puedes eliminar reservaciones CANCELADAS'); return; }
    if (!confirm('¿Eliminar esta reservación? Esta acción es permanente.')) return;
    this.reservas.eliminar(Number(r.id)).subscribe({
      next: () => {
        this.toast('Reserva eliminada');
        this.cargarMias();
        if (this.modalMode === 'agenda') this.cargarSemana();
      }
    });
  }

  cancelar(r: any) {
    if (!r?.id) return;
    if (!confirm('¿Cancelar esta reservación? Debe ser con 2 horas de antelación.')) return;
    this.reservas.cancelar(Number(r.id)).subscribe({
      next: () => {
        this.toast('Reserva cancelada');
        this.cargarMias();
      }
    });
  }

  puedeCancelar(r: any) {
    if (!r?.fecha) return false;
    const start = new Date(r.fecha).getTime();
    const now = Date.now();
    const estado = (r?.estado || '').toString().toUpperCase();
    if (estado === 'PENDIENTE') {
      return now < start; // puede cancelar hasta antes de iniciar
    }
    return now <= (start - 2 * 60 * 60 * 1000); // otras (ej. CONFIRMADA) requieren 2h
  }

  closeModal() {
    this.showModal = false;
  }

  cargarUsos() {
    this.reservas.filtrosUso().subscribe(u => this.usos = u || []);
  }

  buscar(page: number = 0) {
    this.page = page;
    this.reservas.productosReservables({ q: this.q, categoria: this.categoria || undefined, soloConStock: this.soloConStock, page: this.page, size: this.size })
      .subscribe(res => this.data = res);
  }

  seleccionarProducto(p: any) {
    this.productoIdSeleccionado = p?.id ?? null;
    this.selectedProducto = p || null;
    this.activeTab = 'explorar';
    this.fecha = this.nowLocal();
    this.showModal = true;
    this.modalMode = 'reserva';
  }

  reservar() {
    if (!this.productoIdSeleccionado) { alert('Selecciona un producto'); return; }
    if (!this.fecha) { this.fecha = this.nowLocal(); }
    const fechaIso = this.ensureSeconds(this.fecha);
    // clamp a límite dinámico
    const dur = Math.min(this.duracionMinutos || 60, this.maxDurMin || 60);
    this.reservas.crearMe({ productoId: this.productoIdSeleccionado, fecha: fechaIso, duracionMinutos: dur })
      .subscribe(() => {
        this.fecha = '';
        this.toast('Reserva creada ✅');
        this.cargarMias();
        // Cambiar a agenda personal en el mismo modal para permitir cancelar
        this.selectedProducto = null;
        this.productoIdSeleccionado = null;
        this.modalMode = 'agenda';
        if (!this.days?.length) this.initWeek();
        this.cargarSemana();
      });
  }

  cargarMias() {
    this.reservas.mias().subscribe(r => this.miasList = r || []);
  }

  setTab(tab: 'explorar' | 'mias') {
    this.activeTab = tab;
    if (tab === 'mias' && !this.miasList?.length) this.cargarMias();
  }

  private nowLocal(): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = pad(d.getMonth() + 1);
    const dd = pad(d.getDate());
    const hh = pad(d.getHours());
    const mi = pad(d.getMinutes());
    return `${yyyy}-${mm}-${dd}T${hh}:${mi}`; // formato input datetime-local (sin segundos)
  }

  private ensureSeconds(v: string): string {
    // Si viene como yyyy-MM-ddTHH:mm, agregar :00 para cumplir ISO_LOCAL_DATE_TIME
    return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v) ? `${v}:00` : v;
  }

  private toast(msg: string) {
    this.toastMsg = msg;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 1800);
  }

  private cargarCapacidad() {
    // Límite dinámico: 60 * max(horasMaxReserva) de planes activos (tipo HORAS)
    this.perfil.planesMe().subscribe({
      next: (list) => {
        const activos = (list || []).filter((s: any) => s?.estado === 'ACTIVA' && s?.tipo === 'PLAN');
        const maxHoras = activos
          .map((s: any) => Number(s?.horasMaxReserva || 0))
          .reduce((a: number, b: number) => Math.max(a, b), 0);
        this.maxDurMin = Math.max(60, (maxHoras > 0 ? maxHoras * 60 : 60));
      },
      error: () => { this.maxDurMin = 60; }
    });
  }

  // ------- Agenda semanal -------
  private initWeek(base?: Date) {
    const d = base ? new Date(base) : new Date();
    const day = d.getDay(); // 0=dom..6=sab
    const diffToMonday = (day === 0 ? -6 : 1 - day);
    const monday = new Date(d);
    monday.setHours(0,0,0,0);
    monday.setDate(d.getDate() + diffToMonday);
    this.weekStart = monday;
    this.days = Array.from({ length: 7 }, (_, i) => {
      const dt = new Date(monday);
      dt.setDate(monday.getDate() + i);
      const yyyy = dt.getFullYear();
      const mm = (dt.getMonth()+1).toString().padStart(2,'0');
      const dd = dt.getDate().toString().padStart(2,'0');
      const isoDay = `${yyyy}-${mm}-${dd}`;
      const label = dt.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' });
      return { date: dt, label, iso: isoDay };
    });
  }

  prevWeek() { this.initWeek(new Date(this.weekStart.getTime() - 7*24*3600*1000)); this.cargarSemana(); }
  nextWeek() { this.initWeek(new Date(this.weekStart.getTime() + 7*24*3600*1000)); this.cargarSemana(); }

  cargarSemana() {
    if (!this.days?.length) this.initWeek();
    this.ocupados.clear();
    this.reservasByKey.clear();
    this.statusByKey.clear();
    const desde = `${this.days[0].iso}T00:00:00`;
    const pid = this.productoIdSeleccionado ?? undefined;
    this.reservas.semana(desde, pid).subscribe(list => {
      (list || []).forEach((r: any) => {
        const start = new Date(r.fecha);
        const dur = Number(r.duracionMinutos || 60);
        const slots = Math.ceil(dur / 60);
        for (let i=0; i<slots; i++) {
          const dt = new Date(start.getTime() + i*60*60*1000);
          const yyyy = dt.getFullYear();
          const mm = (dt.getMonth()+1).toString().padStart(2,'0');
          const dd = dt.getDate().toString().padStart(2,'0');
          const hh = dt.getHours().toString().padStart(2,'0');
          const key = `${yyyy}-${mm}-${dd}T${hh}:00`;
          this.ocupados.add(key);
          if (!pid) { // vista "Mi agenda": guardar referencia para cancelar
            this.reservasByKey.set(key, r);
          }
          this.statusByKey.set(key, (r?.estado || '').toString().toUpperCase());
        }
      });
    });
  }

  slotKey(dayIso: string, hour: number) {
    const h = hour.toString().padStart(2,'0');
    return `${dayIso}T${h}:00`;
  }

  slotDisabled(dayIso: string, hour: number) {
    const key = this.slotKey(dayIso, hour);
    const now = new Date();
    const dt = new Date(`${key}:00`);
    return this.ocupados.has(key) || dt.getTime() < now.getTime();
  }

  pickSlot(dayIso: string, hour: number) {
    if (!this.productoIdSeleccionado) { alert('Selecciona un producto primero'); return; }
    const hh = hour.toString().padStart(2,'0');
    this.fecha = `${dayIso}T${hh}:00`;
    this.duracionMinutos = 60;
    this.modalMode = 'reserva';
    this.showModal = true;
  }

  toggleAgenda() {
    this.modalMode = 'agenda';
    this.showModal = true;
    this.cargarSemana();
  }

  verAgenda(p: any) {
    if (!p) return;
    this.productoIdSeleccionado = p?.id ?? null;
    this.selectedProducto = p || null;
    this.activeTab = 'explorar';
    this.modalMode = 'agenda';
    this.showModal = true;
    if (!this.days?.length) this.initWeek();
    this.cargarSemana();
  }

  cancelarSlot(dayIso: string, hour: number) {
    const key = this.slotKey(dayIso, hour);
    const r = this.reservasByKey.get(key);
    if (!r?.id) return;
    if (!confirm('¿Cancelar esta reservación? Debe ser con 2 horas de antelación.')) return;
    this.reservas.cancelar(Number(r.id)).subscribe({
      next: () => {
        this.toast('Reserva cancelada');
        this.cargarMias();
        this.cargarSemana();
      }
    });
  }

  verMiAgenda() {
    // Muestra agenda semanal sin producto específico (vista de mis reservas)
    this.selectedProducto = null;
    this.productoIdSeleccionado = null;
    this.modalMode = 'agenda';
    this.showModal = true;
    if (!this.days?.length) this.initWeek();
    this.cargarSemana();
  }

  nameForSlot(dayIso: string, hour: number): string | null {
    // Solo aplicable en "Mi agenda" (sin producto seleccionado)
    if (this.selectedProducto?.id) return null;
    const key = this.slotKey(dayIso, hour);
    const r = this.reservasByKey.get(key);
    const name = r?.producto?.nombre || r?.membresia?.nombre || r?.nombre || null;
    if (!name) return null;
    return name; // mostrar completo
  }
}
