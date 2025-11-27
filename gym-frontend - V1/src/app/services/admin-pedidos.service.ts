import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class AdminPedidosService {
  private base = '/api/admin/pedidos';
  constructor(private http: HttpClient) {}

  listar(p: { page?: number; size?: number; estados?: string[] }) {
    const params: any = { page: p.page ?? 0, size: p.size ?? 20 };
    if (p.estados?.length) params.estados = p.estados;
    return this.http.get<any>(this.base, { params: toHttpParams(params) });
  }

  anular(id: number) { return this.http.patch<any>(`${this.base}/${id}/anular`, {}); }
}
