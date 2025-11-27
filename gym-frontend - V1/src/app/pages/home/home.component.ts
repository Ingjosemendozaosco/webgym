import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  isLoggedIn = false;
  userRole: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.userRole = this.authService.getRole();
    }
  }

  navigateToDashboard() {
    if (this.authService.isAdmin()) {
      this.router.navigate(['/admin']);
      return;
    }
    if (this.authService.isTrabajador()) {
      this.router.navigate(['/trabajador']);
      return;
    }
    // Usuario normal o rol no especificado
    this.router.navigate(['/usuario']);
  }
}
