import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {
  email = '';
  password = '';
  remember = false;

  error = '';
  msg = '';
  loading = false;

  isTyping = false;
  private typingTimer: any;

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    const saved = localStorage.getItem('login.email');
    if (saved) { this.email = saved; this.remember = true; }
  }

  ngOnDestroy(): void { clearTimeout(this.typingTimer); }

  submit() {
    this.error = ''; this.msg = ''; this.loading = true;
    const email = this.email.trim();

    if (this.remember) localStorage.setItem('login.email', email);
    else localStorage.removeItem('login.email');

    this.auth.login({ email, password: this.password }).subscribe({
      next: _ => {
        this.loading = false;
        if (this.auth.isAdmin())      this.router.navigateByUrl('/admin');
        else if (this.auth.isTrabajador()) this.router.navigateByUrl('/trabajador');
        else                          this.router.navigateByUrl('/usuario');
      },
      error: (err) => {
        this.loading = false;
        const msg = err?.error?.message || '';
        if (msg.includes('Cuenta no verificada')) {
          this.router.navigate(['/verify-register'], { queryParams: { email } });
          return;
        }
        this.error = 'Credenciales inválidas';
        setTimeout(() => { if (this.error === 'Credenciales inválidas') this.error = ''; }, 2000);
      }
    });
  }

  onFocus(){ this.isTyping = true; }
  onBlur(){ this.isTyping = false; }
  onTyping(){
    this.isTyping = true;
    if (this.error) this.error = '';
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => this.isTyping = false, 1200);
  }
}
