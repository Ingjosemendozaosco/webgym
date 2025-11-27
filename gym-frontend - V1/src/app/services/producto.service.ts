import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private base = '/api/productos';
  constructor(private http: HttpClient) {}
  categorias() { return this.http.get<string[]>(`${this.base}/categorias`); }
  destacados() { return this.http.get<any[]>(`${this.base}/destacados`); }
  buscar(p: { q?: string; categoria?: string; soloConStock?: boolean; page?: number; size?: number; }) {
    return this.http.get<any>(`${this.base}/search`, { params: toHttpParams(p) });
  }
  popularidad(id: number) { return this.http.post<any>(`${this.base}/${id}/popularidad`, {}); }
}
