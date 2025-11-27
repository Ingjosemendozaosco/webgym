import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { PerfilService } from '../../services/perfil.service';

@Component({
  selector: 'app-perfil-qr',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './perfil-qr.component.html',
  styleUrls: ['./perfil-qr.component.scss']
})
export class PerfilQrComponent {
  nombre = '';
  apellido = '';
  qrToken: string | null = null;
  asistencias: any[] = [];
  personasDentro: number | null = null;

  constructor(private perfil: PerfilService, private router: Router) {
    this.perfil.verMe().subscribe({
      next: (u) => {
        this.nombre = u?.nombre || '';
        this.apellido = u?.apellido || '';
        this.qrToken = u?.qrToken || null;
      },
      error: (err) => {
        if (err?.status === 401) this.router.navigate(['/login']);
      }
    });

    this.perfil.asistenciasMe().subscribe({
      next: (list) => {
        const arr = (list || []).slice();
        arr.sort((a: any, b: any) => {
          const da = a?.fechaHora ? new Date(a.fechaHora).getTime() : 0;
          const db = b?.fechaHora ? new Date(b.fechaHora).getTime() : 0;
          return db - da;
        });
        this.asistencias = arr;
      },
      error: (err) => {
        if (err?.status === 401) this.router.navigate(['/login']);
      }
    });

    this.perfil.concurrenciaGym().subscribe({
      next: (res) => {
        this.personasDentro = (res && typeof res.dentro === 'number') ? res.dentro : null;
      },
      error: (err) => {
        if (err?.status === 401) this.router.navigate(['/login']);
      }
    });
  }

  get qrImageUrl(): string | null {
    if (!this.qrToken) return null;
    const data = encodeURIComponent(this.qrToken);
    return `https://api.qrserver.com/v1/create-qr-code/?size=380x380&data=${data}`;
  }

  get ultimaAsistencia(): any | null {
    const list = this.asistencias || [];
    if (!list.length) return null;
    return list[0];
  }

  get estadoActual(): 'DENTRO' | 'FUERA' | 'SIN_REGISTROS' {
    const u = this.ultimaAsistencia;
    if (!u) return 'SIN_REGISTROS';
    const tipo = (u.tipo || '').toString().toUpperCase();
    if (tipo === 'ENTRADA') return 'DENTRO';
    if (tipo === 'SALIDA') return 'FUERA';
    return 'SIN_REGISTROS';
  }

  volverPerfil() {
    this.router.navigate(['/perfil']);
  }
}
