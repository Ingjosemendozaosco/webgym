import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { VerifyRegisterComponent } from './pages/verify-register/verify-register.component';
import { VerifyLoginComponent } from './pages/verify-login/verify-login.component';
import { RecoverComponent } from './pages/recover/recover.component';
import { RecoverCodeComponent } from './pages/recover-code/recover-code.component';
import { RecoverResetComponent } from './pages/recover-reset/recover-reset.component';
import { AdminComponent } from './pages/admin/admin.component';
import { TrabajadorComponent } from './pages/trabajador/trabajador.component';
import { UsuarioComponent } from './pages/usuario/usuario.component';
import { trabajadorGuard } from './guards/trabajador.guard';
import { authRedirectGuard } from './guards/auth-redirect.guard';
import { ProductosComponent } from './pages/productos/productos.component';
import { ReservasComponent } from './pages/reservas/reservas.component';
import { PromocionesComponent } from './pages/promociones/promociones.component';
import { CarritoComponent } from './pages/carrito/carrito.component';
import { PagosComponent } from './pages/pagos/pagos.component';
import { MembresiasComponent } from './pages/membresias/membresias.component';
import { PlanesComponent } from './pages/planes/planes.component';
import { ConsultasComponent } from './pages/consultas/consultas.component';
import { PerfilComponent } from './pages/perfil/perfil.component';
import { PerfilQrComponent } from './pages/perfil-qr/perfil-qr.component';
import { HistorialComponent } from './pages/historial/historial.component';
import { HistorialPagosComponent } from './pages/historial-pagos/historial-pagos.component';
import { HistorialSuscripcionesComponent } from './pages/historial-suscripciones/historial-suscripciones.component';
import { ComprasConfirmadasComponent } from './pages/compras-confirmadas/compras-confirmadas.component';
import { VideosComponent } from './pages/videos/videos.component';
import { TrabajadorPedidosComponent } from './pages/trabajador-pedidos/trabajador-pedidos.component';
import { TrabajadorPagosComponent } from './pages/trabajador-pagos/trabajador-pagos.component';
import { TrabajadorMembresiasComponent } from './pages/trabajador-membresias/trabajador-membresias.component';
import { TrabajadorPlanesComponent } from './pages/trabajador-planes/trabajador-planes.component';
import { GraciasComponent } from './pages/gracias/gracias.component';
import { TrabajadorReservasComponent } from './pages/trabajador-reservas/trabajador-reservas.component';
import { TrabajadorCheckinComponent } from './pages/trabajador-checkin/trabajador-checkin.component';
import { TrabajadorSoporteComponent } from './pages/trabajador-soporte/trabajador-soporte.component';
import { AdminUsuariosComponent } from './pages/admin-usuarios/admin-usuarios.component';
import { adminGuard } from './guards/admin.guard';
import { AdminProductosComponent } from './pages/admin-productos/admin-productos.component';
import { AdminSuscripcionesComponent } from './pages/admin-suscripciones/admin-suscripciones.component';
import { AdminPedidosComponent } from './pages/admin-pedidos/admin-pedidos.component';
import { AdminReservasComponent } from './pages/admin-reservas/admin-reservas.component';
import { AdminEstadisticasComponent } from './pages/admin-estadisticas/admin-estadisticas.component';
import { AdminPromocionesComponent } from './pages/admin-promociones/admin-promociones.component';

export const routes: Routes = [
  { path: '', redirectTo: 'usuario', pathMatch: 'full' },
  { 
    path: 'register', 
    component: RegisterComponent,
    canActivate: [authRedirectGuard] 
  },
  { 
    path: 'verify-register', 
    component: VerifyRegisterComponent,
    canActivate: [authRedirectGuard] 
  },
  { 
    path: 'login', 
    component: LoginComponent,
    canActivate: [authRedirectGuard] 
  },
  { 
    path: 'recover', 
    component: RecoverComponent,
    canActivate: [authRedirectGuard] 
  },
  { 
    path: 'recover-code', 
    component: RecoverCodeComponent,
    canActivate: [authRedirectGuard] 
  },
  { 
    path: 'recover-reset', 
    component: RecoverResetComponent,
    canActivate: [authRedirectGuard] 
  },
  { 
    path: 'verify-login', 
    component: VerifyLoginComponent,
    canActivate: [authRedirectGuard] 
  },
  { 
    path: 'admin', 
    component: AdminComponent, 
    canActivate: [adminGuard] 
  },
  { 
    path: 'trabajador', 
    component: TrabajadorComponent, 
    canActivate: [trabajadorGuard] 
  },
  { path: 'trabajador/pedidos', component: TrabajadorPedidosComponent, canActivate: [trabajadorGuard] },
  { path: 'trabajador/pagos', component: TrabajadorPagosComponent, canActivate: [trabajadorGuard] },
  { path: 'trabajador/membresias', component: TrabajadorMembresiasComponent, canActivate: [trabajadorGuard] },
  { path: 'trabajador/planes', component: TrabajadorPlanesComponent, canActivate: [trabajadorGuard] },
  { path: 'trabajador/reservas', component: TrabajadorReservasComponent, canActivate: [trabajadorGuard] },
  { path: 'trabajador/checkin', component: TrabajadorCheckinComponent, canActivate: [trabajadorGuard] },
  { path: 'trabajador/soporte', component: TrabajadorSoporteComponent, canActivate: [trabajadorGuard] },
  { path: 'admin/usuarios', component: AdminUsuariosComponent, canActivate: [adminGuard] },
  { path: 'admin/productos', component: AdminProductosComponent, canActivate: [adminGuard] },
  { path: 'admin/suscripciones', component: AdminSuscripcionesComponent, canActivate: [adminGuard] },
  { path: 'admin/pedidos', component: AdminPedidosComponent, canActivate: [adminGuard] },
  { path: 'admin/reservas', component: AdminReservasComponent, canActivate: [adminGuard] },
  { path: 'admin/estadisticas', component: AdminEstadisticasComponent, canActivate: [adminGuard] },
  { path: 'admin/promociones', component: AdminPromocionesComponent, canActivate: [adminGuard] },
  { 
    path: 'usuario', 
    component: UsuarioComponent
  },
  { path: 'productos', component: ProductosComponent },
  { path: 'reservas', component: ReservasComponent },
  { path: 'promociones', component: PromocionesComponent },
  { path: 'carrito', component: CarritoComponent },
  { path: 'pagos', component: PagosComponent },
  { path: 'gracias', component: GraciasComponent },
  { path: 'membresias', component: MembresiasComponent },
  { path: 'planes', component: PlanesComponent },
  { path: 'consultas', component: ConsultasComponent },
  { path: 'videos', component: VideosComponent },
  { path: 'perfil', component: PerfilComponent },
  { path: 'perfil/qr', component: PerfilQrComponent },
  { path: 'historial', component: HistorialComponent },
  { path: 'historial/pagos', component: HistorialPagosComponent },
  { path: 'historial/suscripciones', component: HistorialSuscripcionesComponent },
  { path: 'compras-confirmadas', component: ComprasConfirmadasComponent },
  { 
    path: '**', 
    redirectTo: '' 
  }
];
