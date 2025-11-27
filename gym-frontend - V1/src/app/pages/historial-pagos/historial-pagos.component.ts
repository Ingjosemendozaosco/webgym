import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PerfilService } from '../../services/perfil.service';

@Component({
  selector: 'app-historial-pagos',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './historial-pagos.component.html',
  styleUrls: ['./historial-pagos.component.scss']
})
export class HistorialPagosComponent {
  cargando = true;
  pedidos: any[] = [];

  // Imagen por defecto para pedidos sin foto de producto
  defaultImg = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="260" height="180" viewBox="0 0 260 180"><rect width="260" height="180" fill="%23f3f4f6"/><g fill="none" stroke="%23999" stroke-width="8" stroke-linecap="round" stroke-linejoin="round"><rect x="70" y="60" width="120" height="60" rx="10"/><path d="M80 120h100"/><path d="M95 50v20"/><path d="M165 50v20"/></g></svg>';
  // Imágenes por estado
  pendienteImg = 'https://thumbs.dreamstime.com/b/imagen-de-fondo-del-cielo-%C3%A9pico-amarillo-nubes-c%C3%BAmulonimbus-hermosas-grandes-en-el-hermoso-197443614.jpg';
  confirmadoImg = 'https://wallpapers.com/images/hd/green-galaxy-pxs5p72soy0quywg.jpg';
  canceladoImg = 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRgsrsJzpAH4wGSRdZ94C1CnbvCHbmQmEnXnA&s';

  constructor(private perfil: PerfilService) {
    this.perfil.pedidosMe().subscribe({
      next: (r) => {
        const list = r || [];
        const ordenados = list.slice().sort((a: any, b: any) => {
          const da = a?.fechaPedido ? new Date(a.fechaPedido).getTime() : (a?.fecha ? new Date(a.fecha).getTime() : 0);
          const db = b?.fechaPedido ? new Date(b.fechaPedido).getTime() : (b?.fecha ? new Date(b.fecha).getTime() : 0);
          return db - da; // más recientes primero
        });

        ordenados.forEach(p => {
          try {
            const detalles = (p?.detalles || p?.detallePedidos || []) as any[];
            const d0 = Array.isArray(detalles) && detalles.length ? detalles[0] : null;
            const prod = d0 ? (d0.producto || d0.product || {}) : {};
            p._img = prod.imagen || this.defaultImg;
            p._titulo = prod.nombre || 'Productos del pedido';
            if (Array.isArray(detalles) && detalles.length) {
              p._cantidad = detalles.reduce((acc, d: any) => acc + (Number(d?.cantidad) || 0), 0);
            }
            // clasificar estado para estilos
            const estadoRaw = (p?.estado || '').toString().toUpperCase();
            if (estadoRaw.includes('PEND')) {
              p._estadoKey = 'pendiente';
              p._thumb = this.pendienteImg;
            } else if (estadoRaw.includes('CONF')) {
              p._estadoKey = 'confirmado';
              p._thumb = this.confirmadoImg;
            } else if (estadoRaw.includes('CANCEL') || estadoRaw.includes('ANUL') || estadoRaw.includes('RECH')) {
              p._estadoKey = 'cancelado';
              p._thumb = this.canceladoImg;
            } else {
              p._estadoKey = 'otro';
              p._thumb = p._img || this.defaultImg;
            }
          } catch {
            p._img = this.defaultImg;
          }
        });

        this.pedidos = ordenados;
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }
}
