import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private base = '/api/perfil';
  constructor(private http: HttpClient) {}
  ver(usuarioId: number) { return this.http.get<any>(`${this.base}`, { params: toHttpParams({ usuarioId }) }); }
  editar(usuarioId: number, dto: any) { return this.http.patch<any>(`${this.base}`, dto, { params: toHttpParams({ usuarioId }) }); }
  password(usuarioId: number, nueva: string) { return this.http.patch<any>(`${this.base}/password`, { nueva }, { params: toHttpParams({ usuarioId }) }); }
  pedidos(usuarioId: number) { return this.http.get<any[]>(`${this.base}/pedidos`, { params: toHttpParams({ usuarioId }) }); }
  planes(usuarioId: number) { return this.http.get<any[]>(`${this.base}/planes`, { params: toHttpParams({ usuarioId }) }); }

  // Alternativas sin usuarioId (backend deriva del JWT)
  verMe() { return this.http.get<any>(`${this.base}`); }
  editarMe(dto: any) { return this.http.patch<any>(`${this.base}`, dto); }
  passwordMe(nueva: string) { return this.http.patch<any>(`${this.base}/password`, { nueva }); }
  pedidosMe() { return this.http.get<any[]>(`${this.base}/pedidos`); }
  planesMe() { return this.http.get<any[]>(`${this.base}/planes`); }
  asistenciasMe() { return this.http.get<any[]>(`${this.base}/asistencias`); }
  concurrenciaGym() { return this.http.get<any>(`${this.base}/concurrencia`); }
}
