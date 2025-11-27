import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MembresiasService } from '../../services/membresias.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { SuscripcionesService } from '../../services/suscripciones.service';
import { PagosService } from '../../services/pagos.service';
import { TrabajadorService } from '../../services/trabajador.service';
import { PerfilService } from '../../services/perfil.service';
import { CalificacionesService } from '../../services/calificaciones.service';

@Component({
  selector: 'app-membresias',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './membresias.component.html',
  styleUrls: ['./membresias.component.scss']
})
export class MembresiasComponent {
  data: any[] = [];
  toastMsg = '';
  showToast = false;
  selected: any = null;
  showModal = false;
  showPayModal = false;
  payTarget: any = null;
  payForm: any = { metodoPago: 'ONLINE', monto: 0, comprobanteUrl: '' };
  activeMembresiaIds = new Set<number>();
  processingMembresiaId: number | null = null;
  ratingResumen: { [id: number]: { promedio: number; cantidad: number } } = {};
  ratingForm = { puntuacion: 5, comentario: '' };

  constructor(private api: MembresiasService,
              private auth: AuthService,
              private router: Router,
              private sus: SuscripcionesService,
              private pagos: PagosService,
              private trabajador: TrabajadorService,
              private perfil: PerfilService,
              private calif: CalificacionesService) {
    this.cargar();
    this.cargarActivas();
  }

  private cargarActivas() {
    this.perfil.planesMe().subscribe({
      next: (list) => {
        const now = Date.now();
        const activas = (list || []).filter((s: any) => s?.estado === 'ACTIVA' && this.enVigencia(s, now));
        const ids = activas.filter((s: any) => s?.tipo === 'MEMBRESIA' && s?.membresiaId).map((s: any) => Number(s.membresiaId));
        this.activeMembresiaIds = new Set<number>(ids);
      }
    });
  }

  private enVigencia(s: any, nowMs?: number) {
    const now = nowMs ?? Date.now();
    const ini = s?.fechaInicio ? new Date(s.fechaInicio).getTime() : undefined;
    const fin = s?.fechaFin ? new Date(s.fechaFin).getTime() : undefined;
    if (ini && now < ini) return false;
    if (fin && now > fin) return false;
    return true;
  }

  cargar() {
    const imgA = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT0JP8R2tqZqgskYxc1IGmtdPd9K5oCz2qmpQ&s';
    const imgB = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRovCo_wPv97Kh28kninWuDQMXkBSHWeVszIw&s';
    this.api.listar().subscribe(res => {
      const list = res || [];
      this.data = list.map((m: any) => {
        const nombre: string = (m?.nombre || '').toLowerCase();
        const imagen = m?.imagen || (nombre.includes('cardio') ? imgA : imgB);
        const duracionDias = m?.duracion_dias ?? (m?.duracionMeses ? m.duracionMeses * 30 : m?.duracion ?? 30);
        const beneficiosList = (m?.beneficios ? String(m.beneficios).split(',').map((t: string)=>t.trim()).filter(Boolean) : []);
        const videosList = (m?.videos ? String(m.videos).split(',').map((t: string)=>t.trim()).filter(Boolean) : []);
        const trial = m?.trial_dias ?? m?.trialDias ?? null;
        const descuento = m?.descuento_porcentaje ?? m?.descuentoPorcentaje ?? m?.descuentoPlanPorcentaje ?? 0;
        const tipo = m?.tipo || (trial ? 'FREE_TRIAL' : 'PAGADA');
        const precio = Number(m?.precio ?? 0);
        const precioFinal = Number((precio * (1 - (Number(descuento) || 0) / 100)).toFixed(2));
        return { ...m, imagen, duracionDias, beneficiosList, videosList, trial, descuento, tipo, precioFinal };
      });
      this.cargarRatings();
    });
  }

  adquirir(m: any) {
    // Bloquear cuando no está ACTIVO/ACTIVA
    if (!this.isEnabled(m)) { this.toast('Esta membresía no está disponible.'); return; }
    // Nuevo flujo: registrar intención de suscripción y crear preferencia MP para SUSCRIPCION:<id>
    const monto = Number(m?.precio ?? 0);
    if (this.processingMembresiaId !== null) return;
    this.processingMembresiaId = Number(m?.id ?? 0);
    this.trabajador.iniciarSuscripcion({ membresiaId: m.id, monto }).subscribe({
      next: (s) => {
        const sid = s?.id;
        if (!sid) { this.toast('No se pudo iniciar suscripción'); this.processingMembresiaId = null; return; }
        this.trabajador.crearPreferenciaSuscripcion(sid).subscribe({
          next: (p) => {
            const url = (p as any)?.init_point || (p as any)?.sandbox_init_point;
            if (url) {
              window.location.href = url;
            } else {
              this.toast('No se recibió la URL de pago.');
              this.processingMembresiaId = null;
            }
          },
          error: () => { this.toast('Error al crear preferencia de pago'); this.processingMembresiaId = null; }
        });
      },
      error: (err) => {
        if (err?.status === 401) {
          this.toast('Tu sesión no es válida. Inicia sesión nuevamente.');
        } else {
          this.toast('Error al iniciar suscripción');
        }
        this.processingMembresiaId = null;
      }
    });
  }

  private toast(msg: string) {
    this.toastMsg = msg;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 1800);
  }

  private cargarRatings() {
    (this.data || []).forEach((m: any) => {
      const id = Number(m?.id);
      if (!id) return;
      this.calif.resumenMembresia(id).subscribe(res => {
        this.ratingResumen[id] = res || { promedio: 0, cantidad: 0 };
      });
    });
  }

  enviarCalificacionMembresia() {
    const sel = this.selected;
    if (!sel?.id) return;
    const body = { puntuacion: this.ratingForm.puntuacion, comentario: this.ratingForm.comentario || undefined };
    this.calif.calificarMembresia(Number(sel.id), body).subscribe({
      next: () => {
        this.calif.resumenMembresia(Number(sel.id)).subscribe(r => {
          this.ratingResumen[Number(sel.id)] = r || { promedio: 0, cantidad: 0 };
        });
        this.toast('Gracias por tu reseña');
      },
      error: (err) => {
        if (err?.status === 401) {
          this.toast('Inicia sesión para calificar');
        }
      }
    });
  }

  verMas(m: any) {
    this.selected = m;
    this.showModal = true;
    this.ratingForm = { puntuacion: 5, comentario: '' };
    if (m?.id && !this.ratingResumen[m.id]) {
      this.calif.resumenMembresia(Number(m.id)).subscribe(r => {
        this.ratingResumen[Number(m.id)] = r || { promedio: 0, cantidad: 0 };
      });
    }
  }

  cerrarModal() {
    this.showModal = false;
    this.selected = null;
  }

  openPay(m: any) {
    this.payTarget = m;
    const base = Number(m?.precio ?? 0);
    this.payForm = { metodoPago: 'ONLINE', monto: base, comprobanteUrl: '' };
    this.showPayModal = true;
  }

  cerrarPay() { this.showPayModal = false; this.payTarget = null; }

  pagar() {
    if (!this.payTarget) return;
    this.sus.crear({
      membresiaId: this.payTarget.id,
      metodoPago: this.payForm.metodoPago,
      monto: this.payForm.monto,
      comprobanteUrl: this.payForm.comprobanteUrl
    }).subscribe({
      next: () => {
        this.showPayModal = false;
        this.toast('Pago registrado. Estado: PAGADO_PENDIENTE_APROBACION');
      },
      error: (err) => {
        if (err?.status === 401) {
          this.toast('Tu sesión no es válida. Inicia sesión nuevamente.');
          // this.router.navigate(['/login']); // descomenta si quieres redirigir
        }
      }
    });
  }

  private pagarDirecto(m: any) {
    const titulo = String(m?.nombre ?? 'Membresía');
    const monto = Number(m?.precio ?? 0);
    const userId = this.auth.getUserId();
    this.pagos.crearPreferenciaDirecta(titulo, monto, userId ?? undefined).subscribe({
      next: (res) => {
        const url = (res as any)?.init_point || (res as any)?.sandbox_init_point;
        if (url) {
          window.location.href = url;
        } else {
          this.toast('No se recibió la URL de pago.');
        }
      },
      error: (err) => {
        const msg = err?.error?.message || err?.statusText || 'Error al iniciar pago';
        this.toast(msg);
      }
    });
  }

  // UI helper: considerar activa si estado es ACTIVO o ACTIVA
  isEnabled(m: any): boolean {
    const e = (m?.estado || '').toString().toUpperCase();
    return e === 'ACTIVO' || e === 'ACTIVA';
  }

  hasRating(item: any): boolean {
    if (!item || item.id == null) { return false; }
    const r = this.ratingResumen[item.id as number];
    return !!r && typeof r.cantidad === 'number' && r.cantidad > 0;
  }
}
