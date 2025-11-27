import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  
  if (!auth.isLoggedIn()) {
    return true; // Permitir acceso a la página de login si no está autenticado
  }
  
  // Redirigir según el rol del usuario
  if (auth.isAdmin()) {
    router.navigate(['/admin']);
  } else if (auth.isTrabajador()) {
    router.navigate(['/trabajador']);
  } else {
    // Usuario normal
    router.navigate(['/usuario']);
  }
  
  return false; // No permitir acceso a la página de login
};
