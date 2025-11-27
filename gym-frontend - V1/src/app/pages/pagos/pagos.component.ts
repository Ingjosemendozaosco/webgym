import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { PagosService } from '../../services/pagos.service';
import { AuthService } from '../../services/auth.service';
import { UsuarioService } from '../../services/usuario.service';
import { CarritoService } from '../../services/carrito.service';

@Component({
  selector: 'app-pagos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './pagos.component.html',
  styleUrls: ['./pagos.component.scss']
})
export class PagosComponent {
  metodo: 'PAYPAL' | 'MERCADO_PAGO' | '' = 'MERCADO_PAGO' as any;
  pedidoId: number | null = null;
  pagoId: number | null = null;
  monto: number | null = null;
  referenciaPago = '';
  estadoPago: string | null = null;
  telefono: string = '';
  direccion: string = '';
  private originalTelefono: string = '';
  private originalDireccion: string = '';
  showToast = false;
  toastMsg = '';
  showUpdateModal = false;
  isUpdating = false;
  isPaying = false;

  constructor(private pagos: PagosService, private auth: AuthService, private route: ActivatedRoute, private router: Router, private usuarios: UsuarioService, private carrito: CarritoService) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const resultado = params['resultado'] as string | undefined;
      const status = params['status'] as string | undefined;
      const paymentId = params['payment_id'] as string | undefined;
      const paypalOrderId = (params['paypalOrderId'] as string | undefined) || (params['token'] as string | undefined);
      const paypalPedidoIdParam = params['pedidoId'] as string | undefined;
      const go = (path: string, secs: number) => this.startRedirect(path, secs);
      if (resultado) {
        if (resultado === 'success' || status === 'approved') {
          this.estadoPago = 'APROBADO';
          go('/usuario', 5);
        } else if (resultado === 'pending' || status === 'pending') {
          this.estadoPago = 'PENDIENTE';
        } else if (resultado === 'failure' || status === 'rejected') {
          this.estadoPago = 'RECHAZADO';
          go('/carrito', 6);
        }
      } else if (status || paymentId) {
        if (status === 'approved') { this.estadoPago = 'APROBADO'; go('/usuario', 5); }
        else if (status === 'pending') { this.estadoPago = 'PENDIENTE'; }
        else if (status === 'rejected') { this.estadoPago = 'RECHAZADO'; go('/carrito', 6); }
      }

      if (paypalOrderId && paypalPedidoIdParam) {
        const pedidoIdNum = Number(paypalPedidoIdParam);
        if (!isNaN(pedidoIdNum)) {
          this.isPaying = true;
          this.pagos.capturarPaypal(paypalOrderId, pedidoIdNum).subscribe({
            next: (res) => {
              this.pedidoId = res?.pedidoId ?? this.pedidoId;
              this.pagoId = res?.pagoId ?? this.pagoId;
              this.monto = res?.monto ?? this.monto;
              this.estadoPago = (res?.estadoPago as string) || this.estadoPago || 'APROBADO';
              this.isPaying = false;
              go('/usuario', 5);
            },
            error: () => {
              this.isPaying = false;
            }
          });
        }
      }
    });
    const uid = this.auth.getUserId();
    // Intentar por /me primero
    this.usuarios.me().subscribe({
      next: (u) => {
        if (u) {
          this.telefono = u?.telefono || this.telefono;
          this.direccion = u?.direccion || this.direccion;
          this.originalTelefono = this.telefono || '';
          this.originalDireccion = this.direccion || '';
        }
      },
      error: () => {
        if (uid) {
          this.usuarios.obtener(uid).subscribe(u => {
            this.telefono = u?.telefono || this.telefono;
            this.direccion = u?.direccion || this.direccion;
            this.originalTelefono = this.telefono || '';
            this.originalDireccion = this.direccion || '';
          });
        }
      }
    });
    if (uid) {
      this.carrito.get(uid).subscribe(res => { this.monto = res?.total ?? this.monto; });
    } else {
      this.carrito.getConToken().subscribe(res => { this.monto = res?.total ?? this.monto; });
    }
  }

  // Temporizador de redirección
  redirectIn: number | null = null;
  redirectTo: string | null = null;
  private redirectTimer?: any;
  private startRedirect(path: string, seconds: number) {
    this.redirectTo = path;
    this.redirectIn = seconds;
    if (this.redirectTimer) { clearInterval(this.redirectTimer); }
    this.redirectTimer = setInterval(() => {
      if (this.redirectIn == null) return;
      this.redirectIn = this.redirectIn - 1;
      if (this.redirectIn <= 0) {
        clearInterval(this.redirectTimer);
        this.router.navigate([path]);
      }
    }, 1000);
  }

  iniciar() {
    if (this.isPaying) return;
    const userId = this.auth.getUserId();
    if (!this.telefono || !this.direccion) { alert('Por favor ingresa tu teléfono y dirección de envío.'); return; }
    if (!this.telefonoValido) { alert('El teléfono debe tener exactamente 9 dígitos.'); return; }
    if (this.pendienteActualizar) { alert('Actualiza tus datos de contacto antes de continuar.'); return; }
    if (this.metodo === 'MERCADO_PAGO') {
      this.isPaying = true;
      const crearMP = () => this.pagos.crearPreferenciaMercadoPago(userId ?? undefined).subscribe({
        next: (res: any) => {
          console.log('MP preferencia creada:', res);
          const url = res?.init_point || res?.sandbox_init_point;
          if (url) {
            window.location.href = url;
          } else {
            alert('No se recibió la URL de pago de Mercado Pago.'); this.isPaying = false;
          }
        },
        error: (err) => {
          console.error('Error creando preferencia MP', err);
          const msg = err?.error?.message || err?.statusText || '';
          if (err?.status === 400) {
            alert(`No se pudo iniciar el pago. Detalle: ${msg || 'CARRITO_VACIO'}`); this.isPaying = false;
          } else if (err?.status === 401) {
            alert('Sesión expirada. Inicia sesión de nuevo.'); this.isPaying = false;
          } else {
            alert(`Ocurrió un error al iniciar el pago. ${msg}`); this.isPaying = false;
          }
        }
      });
      if (!userId) {
        crearMP();
      } else {
        crearMP();
      }
      return;
    }
    if (!userId) {
      alert('Debes iniciar sesión para continuar.');
      return;
    }
    if (this.metodo === 'PAYPAL') {
      this.isPaying = true;
      this.pagos.crearOrdenPaypal(userId).subscribe({
        next: (res) => {
          this.pedidoId = res?.pedidoId ?? null;
          this.pagoId = res?.pagoId ?? null;
          const url = res?.approvalUrl as string | undefined;
          if (url) {
            window.location.href = url;
          } else {
            alert('No se recibió la URL de aprobación de PayPal.');
          }
          this.isPaying = false;
        },
        error: () => {
          alert('Ocurrió un error al iniciar el pago con PayPal.');
          this.isPaying = false;
        }
      });
    }
  }

  private toast(msg: string) {
    this.toastMsg = msg;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 1500);
  }

  get pendienteActualizar(): boolean {
    return (this.telefono?.trim() ?? '') !== (this.originalTelefono ?? '')
        || (this.direccion?.trim() ?? '') !== (this.originalDireccion ?? '');
  }

  openUpdateModal() {
    if (!this.telefonoValido) { alert('El teléfono debe tener exactamente 9 dígitos.'); return; }
    if (!this.direccion) { alert('Por favor ingresa tu dirección.'); return; }
    this.showUpdateModal = true;
  }

  confirmUpdate() {
    let userId = this.auth.getUserId();
    if (!this.pendienteActualizar) { this.showUpdateModal = false; return; }
    const doPatch = (uid: number) => {
      this.isUpdating = true;
      this.usuarios.actualizar(uid, { telefono: this.telefono.trim(), direccion: this.direccion.trim() }).subscribe({
        next: () => {
          this.originalTelefono = this.telefono.trim();
          this.originalDireccion = this.direccion.trim();
          this.isUpdating = false;
          this.showUpdateModal = false;
          this.toast('Datos de contacto actualizados');
        },
        error: (err) => {
          this.isUpdating = false;
          const msg = err?.error?.message || err?.statusText || '';
          alert(`No se pudo actualizar tus datos. ${msg}`);
        }
      });
    };
    if (userId) {
      doPatch(userId);
    } else {
      this.usuarios.me().subscribe({
        next: (u: any) => {
          if (u?.id) { doPatch(Number(u.id)); }
          else { alert('Debes iniciar sesión para actualizar tus datos.'); }
        },
        error: () => alert('Debes iniciar sesión para actualizar tus datos.')
      });
    }
  }

  closeModal() { this.showUpdateModal = false; }

  get puedePagar(): boolean {
    return !!this.metodo && this.telefonoValido && !!this.direccion && !this.pendienteActualizar && !this.isPaying;
  }

  get telefonoValido(): boolean {
    return !!this.telefono && /^\d{9}$/.test(this.telefono.trim());
  }

  confirmar() {
    if (!this.pedidoId) return;
    this.pagos.confirmar(this.pedidoId, this.referenciaPago || undefined).subscribe(res => {
      this.estadoPago = res?.estadoPago ?? 'CONFIRMADO';
      alert('Pago confirmado');
    });
  }
}
