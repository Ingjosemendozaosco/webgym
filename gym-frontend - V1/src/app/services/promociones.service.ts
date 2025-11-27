import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class PromocionesService {
  private base = '/api/promociones';
  constructor(private http: HttpClient) {}
  buscar(p: { q?: string; tipo?: 'PRODUCTO'|'RESERVA'|'GENERAL'; evento?: string; soloActivas?: boolean; page?: number; size?: number; }) {
    return this.http.get<any>(`${this.base}`, { params: toHttpParams(p) });
  }
  porCategoria(p: { q?: string; tipo?: 'PRODUCTO'|'RESERVA'|'GENERAL'; categoria?: string; evento?: string; soloActivas?: boolean; page?: number; size?: number; }) {
    return this.http.get<any>(`${this.base}/por-categoria`, { params: toHttpParams(p) });
  }
  flash(minutos = 1440, p: { page?: number; size?: number } = {}) {
    return this.http.get<any>(`${this.base}/flash`, { params: toHttpParams({ minutos, ...p }) });
  }
  categoriasProducto() { return this.http.get<string[]>(`${this.base}/categorias-producto`); }
  activas() { return this.http.get<any[]>(`${this.base}/activas`); }
}
