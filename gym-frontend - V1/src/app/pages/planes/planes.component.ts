import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PlanesService } from '../../services/planes.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { SuscripcionesService } from '../../services/suscripciones.service';
import { PagosService } from '../../services/pagos.service';
import { TrabajadorService } from '../../services/trabajador.service';
import { PerfilService } from '../../services/perfil.service';
import { CalificacionesService } from '../../services/calificaciones.service';

@Component({
  selector: 'app-planes',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './planes.component.html',
  styleUrls: ['./planes.component.scss']
})
export class PlanesComponent {
  data: any[] = [];
  toastMsg = '';
  showToast = false;
  selected: any = null;
  showModal = false;
  showPayModal = false;
  payTarget: any = null;
  payForm: any = { metodoPago: 'ONLINE', monto: 0, comprobanteUrl: '' };
  activePlanIds = new Set<number>();
  processingPlanId: number | null = null;
  ratingResumen: { [id: number]: { promedio: number; cantidad: number } } = {};
  ratingForm = { puntuacion: 5, comentario: '' };

  constructor(private api: PlanesService,
              private auth: AuthService,
              private router: Router,
              private sus: SuscripcionesService,
              private pagos: PagosService,
              private trabajador: TrabajadorService,
              private perfil: PerfilService,
              private calif: CalificacionesService) {
    this.cargar();
    this.cargarActivos();
  }

  cargar() {
    const imgDescuento = 'https://static.vecteezy.com/system/resources/previews/015/452/522/non_2x/discount-icon-in-trendy-flat-style-isolated-on-background-discount-icon-page-symbol-for-your-web-site-design-discount-icon-logo-app-ui-discount-icon-eps-vector.jpg';
    const imgHoras = 'https://media.istockphoto.com/id/1327437190/vector/hourglass-time-icon-vector-illustration-isolated-on-white-background.jpg?s=612x612&w=0&k=20&c=Rp5C2XFS6rpsswAMQciw8iJzhAUklYz1snVmfw_hlso=';
    this.api.listar().subscribe(res => {
      const list = res || [];
      this.data = list.map((p: any) => {
        const nombre: string = (p?.nombre || '').toLowerCase();
        const esHoras = (p?.tipo || '').toString().toUpperCase() === 'HORAS' || !!(p?.horas_max_reserva ?? p?.horasMaxReserva);
        const imagen = p?.imagen || (esHoras ? imgHoras : imgDescuento);
        const duracionDias = p?.duracion_dias ?? p?.duracionDias ?? (p?.duracionMeses ? p.duracionMeses * 30 : p?.duracion ?? 30);
        const descuento = p?.descuento_porcentaje ?? p?.descuentoPorcentaje ?? 0;
        const horasMax = p?.horas_max_reserva ?? p?.horasMaxReserva ?? null;
        const tipo = p?.tipo || (horasMax ? 'HORAS' : (descuento ? 'DESCUENTO' : 'OTRO'));
        const beneficiosList = (p?.beneficio ? String(p.beneficio).split(',').map((t: string)=>t.trim()).filter(Boolean) : []);
        const precio = Number(p?.precio ?? 0);
        const precioFinal = Number((precio * (1 - (Number(descuento) || 0) / 100)).toFixed(2));
        return { ...p, imagen, duracionDias, descuento, horasMax, tipo, beneficiosList, precioFinal };
      });
      this.cargarRatings();
    });
  }

  suscribirse(p: any) {
    // Nuevo flujo para planes: registrar intención y crear preferencia MP para SUSCRIPCION:<id>
    const monto = Number(p?.precio ?? 0);
    if (this.processingPlanId !== null) return;
    this.processingPlanId = Number(p?.id ?? 0);
    this.trabajador.iniciarSuscripcion({ planSuscripcionId: p.id, monto }).subscribe({
      next: (s) => {
        const sid = s?.id;
        if (!sid) { this.toast('No se pudo iniciar suscripción'); this.processingPlanId = null; return; }
        this.trabajador.crearPreferenciaSuscripcion(sid).subscribe({
          next: (pr) => {
            const url = (pr as any)?.init_point || (pr as any)?.sandbox_init_point;
            if (url) {
              window.location.href = url;
            } else {
              this.toast('No se recibió la URL de pago.');
              this.processingPlanId = null;
            }
          },
          error: () => { this.toast('Error al crear preferencia de pago'); this.processingPlanId = null; }
        });
      },
      error: (err) => {
        if (err?.status === 401) {
          this.toast('Tu sesión no es válida. Inicia sesión nuevamente.');
        } else {
          this.toast('Error al iniciar suscripción');
        }
        this.processingPlanId = null;
      }
    });
  }

  private toast(msg: string) {
    this.toastMsg = msg;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 1800);
  }

  private cargarRatings() {
    (this.data || []).forEach((p: any) => {
      const id = Number(p?.id);
      if (!id) return;
      this.calif.resumenPlan(id).subscribe(res => {
        this.ratingResumen[id] = res || { promedio: 0, cantidad: 0 };
      });
    });
  }

  enviarCalificacionPlan() {
    const sel = this.selected;
    if (!sel?.id) return;
    const body = { puntuacion: this.ratingForm.puntuacion, comentario: this.ratingForm.comentario || undefined };
    this.calif.calificarPlan(Number(sel.id), body).subscribe({
      next: () => {
        this.calif.resumenPlan(Number(sel.id)).subscribe(r => {
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

  private cargarActivos() {
    this.perfil.planesMe().subscribe({
      next: (list) => {
        const now = Date.now();
        const activas = (list || []).filter((s: any) => s?.estado === 'ACTIVA' && this.enVigencia(s, now));
        const soloPlanes = activas.filter((s: any) => s?.tipo === 'PLAN');
        const ids = soloPlanes.filter((s: any) => s?.planSuscripcionId).map((s: any) => Number(s.planSuscripcionId));
        this.activePlanIds = new Set<number>(ids);
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

  verMas(p: any) {
    this.selected = p;
    this.showModal = true;
    this.ratingForm = { puntuacion: 5, comentario: '' };
    if (p?.id && !this.ratingResumen[p.id]) {
      this.calif.resumenPlan(Number(p.id)).subscribe(r => {
        this.ratingResumen[Number(p.id)] = r || { promedio: 0, cantidad: 0 };
      });
    }
  }

  cerrarModal() {
    this.showModal = false;
    this.selected = null;
  }

  openPay(p: any) {
    this.payTarget = p;
    const base = Number(p?.precio ?? 0);
    this.payForm = { metodoPago: 'ONLINE', monto: base, comprobanteUrl: '' };
    this.showPayModal = true;
  }

  cerrarPay() { 
    this.showPayModal = false; 
    this.payTarget = null; 
  }

  pagar() {
    if (!this.payTarget) return;
    this.sus.crear({
      planSuscripcionId: this.payTarget.id,
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

  private pagarDirecto(p: any) {
    const titulo = String(p?.nombre ?? 'Plan');
    const monto = Number(p?.precio ?? 0);
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

  // UI helpers: considerar ACTIVO por defecto si no viene estado
  isEnabled(p: any): boolean {
    const e = (p?.estado || '').toString().toUpperCase();
    return e === '' || e === 'ACTIVO';
  }

  hasRating(item: any): boolean {
    if (!item || item.id == null) { return false; }
    const r = this.ratingResumen[item.id as number];
    return !!r && typeof r.cantidad === 'number' && r.cantidad > 0;
  }
}
