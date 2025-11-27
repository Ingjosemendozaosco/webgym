import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  nombre = '';
  apellido = '';
  email = '';
  password = '';
  showPass = false;

  loading = false;
  msg = '';
  error = '';

  constructor(private api: AuthService, private router: Router) {}

  submit(f: NgForm) {
    this.msg = ''; this.error = '';
    if (f.invalid) { return; }
    this.loading = true;

    const dto = {
      nombre: this.nombre.trim(),
      apellido: this.apellido.trim(),
      email: this.email.trim(),
      password: this.password
    };

    // Soporte flexible según cómo se llame en tu AuthService
    const service: any = this.api as any;
    const call =
      service.register?.(dto) ??
      service.signup?.(dto) ??
      service.crearUsuario?.(dto) ??
      service.createUser?.(dto);

    if (!call || typeof call.subscribe !== 'function') {
      this.loading = false;
      this.error = 'No encuentro el método de registro en AuthService (register/signup). Agrégalo o renómbralo.';
      return;
    }

    call.subscribe({
      next: () => {
        this.loading = false;
        this.msg = 'Revisa tu correo para validar la cuenta.';
        this.router.navigate(['/verify-register'], { queryParams: { email: dto.email } });
      },
      error: (e: any) => {
        this.loading = false;
        this.error = e?.error?.message || 'No se pudo registrar. Intenta nuevamente.';
      }
    });
  }
}
