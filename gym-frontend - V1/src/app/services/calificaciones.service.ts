import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class CalificacionesService {
  private base = '/api/calificaciones';

  constructor(private http: HttpClient) {}

  // Productos
  calificarProducto(productoId: number, body: { puntuacion: number; comentario?: string }) {
    return this.http.post(`${this.base}/producto/${productoId}`, body);
  }

  resumenProducto(productoId: number) {
    return this.http.get<{ promedio: number; cantidad: number }>(`${this.base}/producto/${productoId}/resumen`);
  }

  // Membresías
  calificarMembresia(membresiaId: number, body: { puntuacion: number; comentario?: string }) {
    return this.http.post(`${this.base}/membresia/${membresiaId}`, body);
  }

  resumenMembresia(membresiaId: number) {
    return this.http.get<{ promedio: number; cantidad: number }>(`${this.base}/membresia/${membresiaId}/resumen`);
  }

  // Planes
  calificarPlan(planId: number, body: { puntuacion: number; comentario?: string }) {
    return this.http.post(`${this.base}/plan/${planId}`, body);
  }

  resumenPlan(planId: number) {
    return this.http.get<{ promedio: number; cantidad: number }>(`${this.base}/plan/${planId}/resumen`);
  }
}
