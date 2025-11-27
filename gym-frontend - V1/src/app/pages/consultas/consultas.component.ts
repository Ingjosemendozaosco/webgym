import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ConsultasService } from '../../services/consultas.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-consultas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './consultas.component.html',
  styleUrls: ['./consultas.component.scss']
})
export class ConsultasComponent {
  tipo: 'queja' | 'sugerencia' | '' = '' as any;
  asunto = '';
  mensaje = '';
  data: Array<{
    id?: number | string;
    tipo?: 'queja' | 'sugerencia' | string;
    asunto?: string;
    mensaje?: string;
    fecha?: string | Date;
    creadoEn?: string | Date;
    respuesta?: string;
  }> = [];

  constructor(
    private api: ConsultasService,
    private auth: AuthService,
    private router: Router
  ) {
    this.cargar();
  }

  cargar(): void {
    if (!this.auth.isLoggedIn()) { this.router.navigate(['/login']); return; }
    this.api.listarMe().subscribe(res => this.data = res || []);
  }

  enviar(): void {
    if (!this.auth.isLoggedIn()) { this.router.navigate(['/login']); return; }
    const dto = { asunto: this.asunto, mensaje: this.mensaje, tipo: this.tipo || undefined };
    this.api.crearMe(dto).subscribe(() => {
      this.limpiar();
      this.cargar();
      alert('Consulta enviada');
    });
  }

  limpiar(): void {
    this.asunto = '';
    this.mensaje = '';
    this.tipo = '' as any;
  }

  trackById = (_: number, item: any) => item?.id ?? `${item?.tipo}-${item?.asunto}`;
}
