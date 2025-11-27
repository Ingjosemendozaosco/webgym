import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-trabajador-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './trabajador-nav.component.html',
  styleUrls: ['./trabajador-nav.component.scss']
})
export class TrabajadorNavComponent {
  private router = inject(Router);
  auth = inject(AuthService);
  goHome() { this.router.navigate(['/trabajador']); }
  salir() { this.auth.logout(); this.router.navigate(['/login']); }
}
