import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class AdminEstadisticasService {
  private base = '/api/admin/estadisticas';
  constructor(private http: HttpClient) {}

  overview(p: { from: string; to: string; }) {
    return this.http.get<any>(`${this.base}/overview`, { params: toHttpParams(p) });
  }

  ingresosSeries(p: { from: string; to: string; groupBy?: 'day' | 'week' | 'month' }) {
    const params: any = { from: p.from, to: p.to, groupBy: p.groupBy || 'day' };
    return this.http.get<any[]>(`${this.base}/ingresos-series`, { params: toHttpParams(params) });
  }

  pedidosSeries(p: { from: string; to: string; groupBy?: 'day' | 'week' | 'month' }) {
    const params: any = { from: p.from, to: p.to, groupBy: p.groupBy || 'day' };
    return this.http.get<any[]>(`${this.base}/pedidos-series`, { params: toHttpParams(params) });
  }

  reservasSeries(p: { from: string; to: string; groupBy?: 'day' | 'week' | 'month' }) {
    const params: any = { from: p.from, to: p.to, groupBy: p.groupBy || 'day' };
    return this.http.get<any[]>(`${this.base}/reservas-series`, { params: toHttpParams(params) });
  }

  ultimosPedidos(limit = 10) {
    return this.http.get<any[]>(`${this.base}/ultimos-pedidos`, { params: toHttpParams({ limit }) });
  }

  tops(p: { from: string; to: string; limit?: number }) {
    const params: any = { from: p.from, to: p.to, limit: p.limit || 5 };
    return this.http.get<any>(`${this.base}/tops`, { params: toHttpParams(params) });
  }
}
