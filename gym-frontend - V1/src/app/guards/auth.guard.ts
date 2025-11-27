import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  
  if (!auth.isLoggedIn()) {
    // Si no está autenticado, permitir el acceso a la ruta
    return true;
  }
  
  // Si está autenticado, redirigir según el rol
  const role = auth.getRole();
  
  if (state.url === '/' || state.url === '') {
    // Si está en la raíz, permitir el acceso al home
    return true;
  }
  
  // Redirigir según el rol si intenta acceder a rutas protegidas
  if (role === 'admin') {
    router.navigate(['/admin']);
  } else if (role === 'trabajador') {
    router.navigate(['/trabajador']);
  } else if (role === 'usuario' || !role) {
    router.navigate(['/usuario']);
  }
  
  return false;
};
