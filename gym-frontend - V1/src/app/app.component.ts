import { Component, OnInit, OnDestroy, inject, Inject } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { AuthService } from './services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  auth = inject(AuthService);
  private router = inject(Router); 
  constructor(@Inject(DOCUMENT) private doc: Document) {}

  mobileOpen = false;
  toggle() { this.mobileOpen = !this.mobileOpen; }
  close()  { this.mobileOpen = false; }

  // Soporte para fijar manualmente userId cuando el token no lo trae
  showUserIdPrompt = false;
  manualUserId: string = '';
  get needsUserId(): boolean {
    return !!this.auth.getToken() && (this.auth.getUserId() == null);
  }
  saveUserId() {
    const n = Number(this.manualUserId);
    if (!Number.isFinite(n) || n <= 0) return;
    this.auth.setManualUserId(n);
    this.showUserIdPrompt = false;
  }

  logout() {
    this.auth.logout();         
    this.close();             
    this.router.navigate(['/login']);  
  }

  isAdminLanding(): boolean {
    const url = this.router.url.split('?')[0];
    return url.startsWith('/admin') || url.startsWith('/trabajador');
  }

  isAuthRoute(): boolean {
    const url = this.router.url.split('?')[0];
    return url === '/login' 
      || url === '/register' 
      || url === '/verify-login' 
      || url === '/verify-register'
      || url === '/recover'
      || url === '/recover-code'
      || url === '/recover-reset';
  }

  isFullBleedRoute(): boolean {
    const url = this.router.url.split('?')[0];
    return this.isAuthRoute() 
      || url === '/usuario' 
      || url === '/membresias' 
      || url === '/planes'
      || url === '/productos'
      || url === '/carrito'
      || url === '/pagos'
      || url === '/reservas'
      || url === '/consultas';
  }

  private routerSub?: Subscription;

  ngOnInit(): void {
    this.toggleBodyScroll(this.isAuthRoute());
    this.showUserIdPrompt = this.needsUserId;
    this.routerSub = this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => {
        this.toggleBodyScroll(this.isAuthRoute());
        this.showUserIdPrompt = this.needsUserId;
      });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
    this.toggleBodyScroll(false);
  }

  private toggleBodyScroll(noScroll: boolean) {
    const body = this.doc?.body;
    if (!body) return;
    if (noScroll) body.classList.add('no-scroll');
    else body.classList.remove('no-scroll');
  }
}
