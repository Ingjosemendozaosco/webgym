import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private base = '/api/dashboard';
  constructor(private http: HttpClient) {}
  resumen(usuarioId: number) { return this.http.get<any>(`${this.base}/${usuarioId}`); }
}
