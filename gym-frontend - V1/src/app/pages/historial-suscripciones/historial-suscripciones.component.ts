import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PerfilService } from '../../services/perfil.service';

@Component({
  selector: 'app-historial-suscripciones',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './historial-suscripciones.component.html',
  styleUrls: ['./historial-suscripciones.component.scss']
})
export class HistorialSuscripcionesComponent {
  cargando = true;
  suscripciones: any[] = [];

  constructor(private perfil: PerfilService) {
    this.perfil.planesMe().subscribe({
      next: (r) => {
        const list = r || [];
        const ordenadas = list.slice().sort((a: any, b: any) => {
          const da = a?.fechaInicio ? new Date(a.fechaInicio).getTime() : 0;
          const db = b?.fechaInicio ? new Date(b.fechaInicio).getTime() : 0;
          return db - da; // más recientes primero
        });

        ordenadas.forEach(s => {
          const raw = (s?.estado || '').toString().toUpperCase();
          if (raw.includes('ACTIV')) {
            s._estadoKey = 'activa';
          } else if (raw.includes('PEND')) {
            s._estadoKey = 'pendiente';
          } else if (raw.includes('CANCEL') || raw.includes('RECH') || raw.includes('EXPIR')) {
            s._estadoKey = 'cancelada';
          } else {
            s._estadoKey = 'otro';
          }
        });

        this.suscripciones = ordenadas;
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }
}
