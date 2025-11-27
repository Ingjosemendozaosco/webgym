import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'app-verify-register',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './verify-register.component.html',
    styleUrls: ['./verify-register.component.scss']
    })
    export class VerifyRegisterComponent {
    email = '';
    code = '';
    msg = '';
    loading = false;
    showCode = false;

    constructor(private route: ActivatedRoute, private router: Router, private auth: AuthService) {
        const qpEmail = this.route.snapshot.queryParamMap.get('email');
        if (qpEmail) this.email = qpEmail;
    }

    submit() {
        if (!this.email || !this.code) { this.msg='Completa los campos'; return; }
        this.loading = true; this.msg = '';
        this.auth.registerConfirm(this.email, this.code).subscribe({
        next: _ => { this.loading=false; this.msg='✅ Cuenta creada'; setTimeout(()=> this.router.navigateByUrl('/login'), 700); },
        error: _ => { this.loading=false; this.msg='❌ Código inválido o expirado'; }
        });
    }

    toggleShow() { this.showCode = !this.showCode; }
}
