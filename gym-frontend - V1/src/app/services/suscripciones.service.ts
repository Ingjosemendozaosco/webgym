import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class SuscripcionesService {
  private base = '/api/suscripciones';
  constructor(private http: HttpClient) {}

  crear(body: {
    membresiaId?: number;
    planSuscripcionId?: number;
    metodoPago?: string;
    monto?: number;
    comprobanteUrl?: string;
  }) {
    return this.http.post<any>(`${this.base}`, body);
  }

  mias() {
    return this.http.get<any[]>(`${this.base}/mias`);
  }
}
