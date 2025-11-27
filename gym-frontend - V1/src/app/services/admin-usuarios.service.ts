import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class AdminUsuariosService {
  private base = '/api/admin/usuarios';
  constructor(private http: HttpClient) {}
  listar(p: { page?: number; size?: number; q?: string; rol?: string; activo?: boolean | null; }) {
    const params: any = { page: p.page ?? 0, size: p.size ?? 20 };
    if (p.q) params.q = p.q;
    if (p.rol) params.rol = p.rol;
    if (p.activo !== undefined && p.activo !== null) params.activo = p.activo;
    return this.http.get<any>(this.base, { params: toHttpParams(params) });
  }
  setRol(id: number, rol: string) { return this.http.patch<any>(`${this.base}/${id}/rol`, { rol }); }
  setActivo(id: number, activo: boolean) { return this.http.patch<any>(`${this.base}/${id}/activo`, { activo }); }
}
