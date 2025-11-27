import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class AdminSuscripcionesService {
  private basePlanes = '/api/planes';
  private baseMembresias = '/api/membresias';
  constructor(private http: HttpClient) {}

  // Planes
  listarPlanes() { return this.http.get<any[]>(this.basePlanes); }
  obtenerPlan(id: number) { return this.http.get<any>(`${this.basePlanes}/${id}`); }
  crearPlan(body: any) { return this.http.post<any>(this.basePlanes, body); }
  actualizarPlan(id: number, body: any) { return this.http.put<any>(`${this.basePlanes}/${id}`, body); }
  eliminarPlan(id: number) { return this.http.delete<void>(`${this.basePlanes}/${id}`); }

  // Membresías
  listarMembresias() { return this.http.get<any[]>(this.baseMembresias); }
  obtenerMembresia(id: number) { return this.http.get<any>(`${this.baseMembresias}/${id}`); }
  crearMembresia(body: any) { return this.http.post<any>(this.baseMembresias, body); }
  actualizarMembresia(id: number, body: any) { return this.http.put<any>(`${this.baseMembresias}/${id}`, body); }
  eliminarMembresia(id: number) { return this.http.delete<void>(`${this.baseMembresias}/${id}`); }

  // Suscriptores
  suscriptoresMembresia(id: number) { return this.http.get<any[]>(`${this.baseMembresias}/${id}/suscriptores`); }
  suscriptoresPlan(id: number) { return this.http.get<any[]>(`${this.basePlanes}/${id}/suscriptores`); }
}
