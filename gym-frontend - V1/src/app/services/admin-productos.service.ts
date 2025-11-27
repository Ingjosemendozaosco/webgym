import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class AdminProductosService {
  private base = '/api/productos';
  constructor(private http: HttpClient) {}

  listar() { return this.http.get<any[]>(this.base); }
  obtener(id: number) { return this.http.get<any>(`${this.base}/${id}`); }
  crear(p: any) { return this.http.post<any>(this.base, p); }
  actualizar(id: number, p: any) { return this.http.put<any>(`${this.base}/${id}`, p); }
  eliminar(id: number) { return this.http.delete<void>(`${this.base}/${id}`); }

  buscar(opts: { q?: string; categoria?: string; soloConStock?: boolean; mostrarTodos?: boolean; disponible?: boolean | undefined; page?: number; size?: number; }) {
    const params = toHttpParams({
      q: opts.q ?? '',
      categoria: opts.categoria ?? undefined,
      soloConStock: opts.soloConStock ?? false,
      mostrarTodos: opts.mostrarTodos ?? false,
      disponible: opts.disponible,
      page: opts.page ?? 0,
      size: opts.size ?? 12,
    });
    return this.http.get<any>(`${this.base}/search`, { params });
  }

  categorias() { return this.http.get<string[]>(`${this.base}/categorias`); }
}
