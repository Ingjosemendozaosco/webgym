import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { toHttpParams } from './http.util';

@Injectable({ providedIn: 'root' })
export class MembresiasService {
  private base = '/api/membresias';
  constructor(private http: HttpClient) {}
  listar() { return this.http.get<any[]>(`${this.base}`); }
  obtener(id: number) { return this.http.get<any>(`${this.base}/${id}`); }
  adquirir(id: number, usuarioId: number, monto?: number) {
    return this.http.post<any>(`${this.base}/${id}/adquirir`, null, { params: toHttpParams({ usuarioId, monto }) });
  }
}
