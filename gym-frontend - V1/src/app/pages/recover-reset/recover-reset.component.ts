import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-recover-reset',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recover-reset.component.html',
  styleUrls: ['./recover-reset.component.scss']
})
export class RecoverResetComponent {
  email = '';
  private code = '';
  newPassword = '';
  confirmPassword = '';
  showPass = false;
  showCode = false;

  loading = false;
  msg = '';
  error = '';

  constructor(private route: ActivatedRoute, private router: Router, private auth: AuthService) {
    const qpEmail = this.route.snapshot.queryParamMap.get('email');
    if (qpEmail) this.email = qpEmail;
    const qpCode = this.route.snapshot.queryParamMap.get('code');
    if (qpCode) this.code = qpCode;
  }

  submit(f: NgForm) {
    this.msg = '';
    this.error = '';
    if (f.invalid || !this.email.trim() || !this.code || !this.newPassword || !this.confirmPassword) return;

    if (this.newPassword !== this.confirmPassword) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }

    this.loading = true;
    const email = this.email.trim();
    const code = this.code.trim();
    const newPassword = this.newPassword;

    this.auth.recoverConfirm(email, code, newPassword).subscribe({
      next: res => {
        this.loading = false;
        this.msg = res.message || 'Contraseña actualizada. Ya puedes iniciar sesión.';
        setTimeout(() => this.router.navigate(['/login']), 2500);
      },
      error: (e: any) => {
        this.loading = false;
        this.error = e?.error?.message || 'No se pudo actualizar la contraseña. Revisa el código.';
      }
    });
  }

  toggleShowPass() { this.showPass = !this.showPass; }
  toggleShowCode() { this.showCode = !this.showCode; }
}
