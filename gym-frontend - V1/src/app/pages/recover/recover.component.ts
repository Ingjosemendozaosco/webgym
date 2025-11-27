import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-recover',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recover.component.html',
  styleUrls: ['./recover.component.scss']
})
export class RecoverComponent {
  email = '';

  loading = false;
  msg = '';
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  sendCode(f: NgForm) {
    this.msg = '';
    this.error = '';
    if (f.invalid || !this.email.trim()) return;
    this.loading = true;
    const email = this.email.trim();
    this.auth.recoverInit(email).subscribe({
      next: res => {
        this.loading = false;
        this.msg = res.message || 'Se envió un código de recuperación a tu correo.';
        this.router.navigate(['/recover-code'], { queryParams: { email } });
      },
      error: (e: any) => {
        this.loading = false;
        this.error = e?.error?.message || 'No se pudo enviar el código. Intenta nuevamente.';
      }
    });
  }
}
