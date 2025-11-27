import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class AdminPromocionesService {
  private base = '/api/promociones';
  constructor(private http: HttpClient) {}

  listar(soloActivas = false, page=0, size=50) { return this.http.get<any>(`${this.base}`, { params: toHttpParams({ soloActivas, page, size }) }); }
  listarContenido(soloActivas=false, page=0, size=50) { return this.listar(soloActivas, page, size); }
  activas() { return this.http.get<any[]>(`${this.base}/activas`); }
  obtener(id: number) { return this.http.get<any>(`${this.base}/${id}`); }
  crear(body: any) { return this.http.post<any>(`${this.base}`, body); }
  actualizar(id: number, body: any) { return this.http.put<any>(`${this.base}/${id}`, body); }
  eliminar(id: number) { return this.http.delete<void>(`${this.base}/${id}`); }
}
