import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class RecordatoriosService {
  private base = '/api/recordatorios';
  constructor(private http: HttpClient) {}

  enviar(usuarioId: number) {
    return this.http.post<any>(`${this.base}/${usuarioId}`, {});
  }
}
