import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-recover-code',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recover-code.component.html',
  styleUrls: ['./recover-code.component.scss']
})
export class RecoverCodeComponent {
  email = '';
  code = '';
  loading = false;
  msg = '';
  error = '';

  constructor(private route: ActivatedRoute, private router: Router, private auth: AuthService) {
    const qpEmail = this.route.snapshot.queryParamMap.get('email');
    if (qpEmail) this.email = qpEmail;
  }

  submit(f: NgForm) {
    this.msg = '';
    this.error = '';
    if (f.invalid || !this.email.trim() || !this.code.trim()) return;
    this.loading = true;
    const email = this.email.trim();
    const code = this.code.trim();

    this.auth.recoverValidate(email, code).subscribe({
      next: _ => {
        this.loading = false;
        this.router.navigate(['/recover-reset'], { queryParams: { email, code } });
      },
      error: (e: any) => {
        this.loading = false;
        this.error = e?.error?.message || 'Código inválido o expirado.';
      }
    });
  }
}
