import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

// 👇 Importa DIRECTIVAS y PIPES standalone explícitamente
import { NgIf, NgFor, DatePipe, DecimalPipe, CommonModule } from '@angular/common';

import { AuthService } from '../../services/auth.service';
import { DashboardService } from '../../services/dashboard.service';
import { PromocionesService } from '../../services/promociones.service';

@Component({
  selector: 'app-usuario',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './usuario.component.html',
  styleUrls: ['./usuario.component.scss']
})
export class UsuarioComponent implements OnInit {
  // ===== Datos que usa tu template =====
  proximasReservas: any[] = [];
  carritoResumen: any = null;
  planesActivos: any[] = [];

  // Productos destacados (del API) — el template espera "productosDestacados"
  productosDestacados: any[] = [];
  placeholderImg = 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800';

  // Servicios (mock local; cámbialo por datos del backend cuando lo tengas)
  servicios = [
    {
      titulo: 'Entrenamiento guiado',
      descripcion: 'Rutinas personalizadas con seguimiento por coach.',
      icono: `<?xml version="1.0"?><svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 12h-2a4 4 0 0 0-8 0H4v2h6a4 4 0 0 0 8 0h2v-2Z"/></svg>`
    },
    {
      titulo: 'Clases grupales',
      descripcion: 'HIIT, funcional, full body y más con cupos diarios.',
      icono: `<?xml version="1.0"?><svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm-7 9a7 7 0 0 1 14 0Z"/></svg>`
    },
    {
      titulo: 'Nutrición',
      descripcion: 'Planes alimenticios y control de progreso.',
      icono: `<?xml version="1.0"?><svg viewBox="0 0 24 24" fill="currentColor"><path d="M7 3h10v2H7zm0 4h10v2H7zM5 11h14v2H5zm2 4h10v2H7z"/></svg>`
    }
  ];

  // Contacto / sede (con lo que me enviaste)
  direccion = {
    linea1: 'Angels gym',
    linea2: 'Av. (referencia) — Piso 2',
    ciudad: 'Ica,',
    region: 'Perú'
  };

  // Horarios: Lun–Vie 8:00–23:00 / Sáb 8:00–21:00 (tu texto: “abre de lunes a viernes de 8 hasta las 11 y sábados de 8 a 9”)
  horario = [
    { dia: 'Lun – Vie', horas: '08:00 – 23:00' },
    { dia: 'Sábado',    horas: '08:00 – 21:00' }
  ];

  telefonos = ['918 854 055'];
  correo = 'angelsgymica@gmail.com';
  redes = {
    facebook: 'https://www.facebook.com/fitnessequipmentperu/?locale=es_LA'
  };

  // (Opcional) Si sigues usando promos en otro lado
  promosActivas: any[] = [];

  constructor(
    private authService: AuthService,
    private router: Router,
    private dashboardService: DashboardService,
    private promosService: PromocionesService
  ) {}

  ngOnInit() {
    this.redirectBasedOnRole();

    const userId = this.authService.getUserId();
    if (userId) {
      this.dashboardService.resumen(userId).subscribe(data => {
        // El backend te devuelve "destacados": lo mapeamos a "productosDestacados" para el template
        this.productosDestacados = data?.destacados || [];
        this.proximasReservas   = data?.proximasReservas || [];
        this.carritoResumen     = data?.carrito || null;
        this.planesActivos      = data?.planesActivos || [];
      });
    }

    this.promosService.activas().subscribe(p => this.promosActivas = p || []);
  }

  private redirectBasedOnRole() {
    const role = this.authService.getRole();
    if (role === 'admin') {
      this.router.navigate(['/admin']);
    } else if (role === 'trabajador') {
      this.router.navigate(['/trabajador']);
    }
  }

  addToCart(p: any) {
    // Integra con tu CartService real
    console.log('Agregar al carrito', p);
  }
}
