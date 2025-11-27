import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-verify-login',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './verify-login.component.html',
    styleUrls: ['./verify-login.component.scss']
    })
    export class VerifyLoginComponent {
    email = '';
    code = '';
    msg = '';
    loading = false;

    constructor(private route: ActivatedRoute, private router: Router, private auth: AuthService) {
        const qpEmail = this.route.snapshot.queryParamMap.get('email');
        if (qpEmail) this.email = qpEmail;
    }

    submit() {
        if (!this.email || !this.code) { this.msg = 'Completa los campos'; return; }
        this.loading = true; this.msg = '';
            this.auth.loginConfirm(this.email, this.code).subscribe({
            next: (res: any) => {
                this.loading = false;
                if (res?.token) {
                this.auth.setToken(res.token);
                }
                this.router.navigateByUrl('/');
            },
            error: _ => {
                this.loading = false;
                this.msg = '❌ Código inválido o expirado';
            }
            });
    }

}
