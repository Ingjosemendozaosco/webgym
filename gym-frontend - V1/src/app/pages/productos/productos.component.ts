import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductoService } from '../../services/producto.service';
import { CarritoService } from '../../services/carrito.service';
import { AuthService } from '../../services/auth.service';
import { PromocionesService } from '../../services/promociones.service';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { CalificacionesService } from '../../services/calificaciones.service';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './productos.component.html',
  styleUrls: ['./productos.component.scss']
})
export class ProductosComponent {
  q = '';
  categoria = '';
  soloConStock = false;
  categorias: string[] = [];
  page = 0;
  size = 10000; // cargar todo en una sola página
  data: any = { content: [], totalElements: 0 };
  adding = false;
  showSuccess = false;
  // Imagen por defecto (SVG inline en data URL, ícono de mancuerna)
  defaultImg = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="200" viewBox="0 0 300 200"><rect width="300" height="200" fill="%23f3f4f6"/><g fill="none" stroke="%23999" stroke-width="10" stroke-linecap="round" stroke-linejoin="round"><path d="M80 120h140"/><path d="M70 100v40"/><path d="M60 105v30"/><path d="M230 100v40"/><path d="M240 105v30"/></g></svg>';
  selected: any = null;
  featuredId: number | null = null;

  promos: any[] = [];
  ratingResumen: { [id: number]: { promedio: number; cantidad: number } } = {};
  ratingForm = { puntuacion: 5, comentario: '' };

  constructor(private productos: ProductoService,
              private carrito: CarritoService,
              private auth: AuthService,
              private promosService: PromocionesService,
              private router: Router,
              private calif: CalificacionesService) {
    this.cargarCategorias();
    this.cargarPromos();
    this.buscar();
  }

  cargarCategorias() {
    this.productos.categorias().subscribe(c => {
      this.categorias = (c || []).slice().sort((a: string, b: string) => a.localeCompare(b));
    });
  }

  buscar(page: number = 0) {
    this.page = page;
    this.productos.buscar({ q: this.q, categoria: this.categoria || undefined, soloConStock: this.soloConStock, page: this.page, size: this.size })
      .subscribe(res => {
        this.data = res;
        const items = (res?.content || []) as any[];
        if (items.length) {
          const max = items.reduce((a, b) => ((a?.popularidad || 0) >= (b?.popularidad || 0) ? a : b));
          this.featuredId = max?.id ?? null;
        } else {
          this.featuredId = null;
        }
        this.aplicarPromosEnLista();
        this.cargarRatingsParaLista(items);
      });
  }

  comprar(p: any) {
    const obs = this.carrito.agregarConToken(p.id, 1);
    this.adding = true;
    obs.pipe(finalize(() => { this.adding = false; }))
      .subscribe({
      next: () => {
        this.showSuccess = true;
        setTimeout(() => this.showSuccess = false, 1500);
      },
      error: (err) => {
        if (err?.status === 401) {
          alert('Inicia sesión para comprar');
          this.router.navigate(['/login']);
        }
      }
    });
  }

  like(p: any) {
    this.productos.popularidad(p.id).subscribe(() => {
      if (typeof p.popularidad === 'number') p.popularidad++;
    });
  }

  openDetalles(p: any) {
    this.selected = p;
    this.ratingForm = { puntuacion: 5, comentario: '' };
    if (p?.id && !this.ratingResumen[p.id]) {
      this.cargarRatingsParaLista([p]);
    }
  }
  closeDetalles() { this.selected = null; }

  getCards(): any[] {
    const items = (this.data?.content || []) as any[];
    if (!items.length) return [];
    if (!this.featuredId) return items;
    const featured = items.find(i => i.id === this.featuredId);
    const others = items.filter(i => i.id !== this.featuredId);
    return featured ? [featured, ...others] : items;
  }

  private cargarPromos() {
    this.promosService.activas().subscribe(list => {
      this.promos = list || [];
      this.aplicarPromosEnLista();
    });
  }

  private aplicarPromosEnLista() {
    const items = (this.data?.content || []) as any[];
    if (!items.length) return;
    const promos = this.promos || [];
    for (const p of items) {
      const base = Number(p?.precio ?? 0);
      let prodPct = Number(p?.descuento ?? 0);
      if (isNaN(prodPct)) prodPct = 0;
      let promoPct = 0;

      for (const promo of promos) {
        if (!promo) continue;
        const tipo = promo.tipo as string | undefined;
        let aplica = false;
        if (tipo === 'GENERAL') {
          // Promos generales aplican a todos los productos
          aplica = true;
        } else if (tipo === 'PRODUCTO') {
          const mismaCategoria = promo.categoria && p.categoria && promo.categoria === p.categoria;
          let explicit = false;
          if (Array.isArray(promo.productos)) {
            explicit = promo.productos.some((pr: any) => pr && pr.id === p.id);
          }
          // Para PRODUCTO, solo si coincide categoría o está en la lista de productos
          aplica = !!(mismaCategoria || explicit);
        }

        if (!aplica) continue;
        const d = Number(promo.descuentoPorcentaje ?? 0);
        if (!isNaN(d) && d > promoPct) {
          promoPct = d;
        }
      }

      let best = Math.max(prodPct, Math.max(0, Math.min(100, promoPct)));
      if (best < 0) best = 0;
      p._prodPct = prodPct;
      p._promoPct = promoPct;
      p._bestPct = best;
      const finalPrice = best > 0 ? (base - (base * best / 100)) : base;
      p._precioFinal = finalPrice;
    }
  }

  private cargarRatingsParaLista(items: any[]) {
    (items || []).forEach((p: any) => {
      const id = Number(p?.id);
      if (!id) return;
      this.calif.resumenProducto(id).subscribe({
        next: (res) => {
          this.ratingResumen[id] = res || { promedio: 0, cantidad: 0 };
        }
      });
    });
  }

  enviarCalificacionProducto() {
    const sel = this.selected;
    if (!sel?.id) return;
    const body = { puntuacion: this.ratingForm.puntuacion, comentario: this.ratingForm.comentario || undefined };
    this.calif.calificarProducto(Number(sel.id), body).subscribe({
      next: () => {
        this.calif.resumenProducto(Number(sel.id)).subscribe(r => {
          this.ratingResumen[Number(sel.id)] = r || { promedio: 0, cantidad: 0 };
        });
      },
      error: (err) => {
        if (err?.status === 401) {
          alert('Inicia sesión para calificar');
        }
      }
    });
  }

  hasRating(item: any): boolean {
    if (!item || item.id == null) { return false; }
    const r = this.ratingResumen[item.id as number];
    return !!r && typeof r.cantidad === 'number' && r.cantidad > 0;
  }
}
