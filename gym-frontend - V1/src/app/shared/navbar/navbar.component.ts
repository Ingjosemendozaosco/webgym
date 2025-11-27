// src/app/shared/navbar/navbar.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SuscripcionesService } from '../../services/suscripciones.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  mobileOpen = false;
  hasActiveMembership = false;
  showNavbar = true;

  constructor(public auth: AuthService, private router: Router, private susService: SuscripcionesService) {}

  ngOnInit(): void {
    const check = () => {
      if (!this.auth.isLoggedIn()) { this.hasActiveMembership = false; return; }
      this.susService.mias().subscribe({
        next: (list) => {
          const arr = Array.isArray(list) ? list : [];
          const ahora = Date.now();
          const enVigencia = (s: any) => {
            try {
              const ini = s?.fechaInicio ? new Date(s.fechaInicio).getTime() : undefined;
              const fin = s?.fechaFin ? new Date(s.fechaFin).getTime() : undefined;
              if (ini && ahora < ini) return false;
              if (fin && ahora > fin) return false;
              return true;
            } catch { return true; }
          };
          this.hasActiveMembership = arr.some((s: any) => {
            const tipo = (s?.tipo || '').toString().toUpperCase();
            const estados = [s?.estado, s?.status, s?.estadoSuscripcion, s?.estadoMembresia]
              .map(v => (v ?? '').toString().toUpperCase());
            const esActiva = estados.includes('ACTIVA');
            const esMemb = tipo === 'MEMBRESIA' || !!s?.membresiaId || !!s?.membresia || !!s?.membresiaNombre;
            return esMemb && esActiva && enVigencia(s);
          });
          console.log('[Navbar] suscripciones.mias()', { count: arr.length, hasActive: this.hasActiveMembership, muestra: arr.slice(0,3) });
        },
        error: (err) => {
          console.warn('[Navbar] Error consultando suscripciones', err);
          this.hasActiveMembership = false;
        }
      });
    };

    const updateVisibility = (url: string) => {
      // Ocultar navbar en la pantalla de recuperación de contraseña
      this.showNavbar = !url.includes('/recover');
    };

    updateVisibility(this.router.url);
    check();
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(e => {
        updateVisibility(e.urlAfterRedirects);
        check();
      });
  }

  toggle() { this.mobileOpen = !this.mobileOpen; }
  close() { this.mobileOpen = false; }

  logout() {
    this.auth.logout();     
    this.close();          
    this.router.navigate(['/login']);  
  }
}
