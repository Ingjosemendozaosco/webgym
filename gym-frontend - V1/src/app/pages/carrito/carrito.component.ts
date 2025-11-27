import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CarritoService } from '../../services/carrito.service';
import { ProductoService } from '../../services/producto.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './carrito.component.html',
  styleUrls: ['./carrito.component.scss']
})
export class CarritoComponent {
  data: any = { items: [], subtotal: 0, descuentoPlanPorcentaje: 0, descuentoPlanMonto: 0, total: 0, moneda: 'PEN' };
  showToast = false;
  toastMsg = '';
  cargandoAccion = false;
  productosDestacados: any[] = [];
  carouselItems: any[] = [];
  private carouselTimer?: any;
  carouselFading = false;
  private fetchingMore = false;

  constructor(private carrito: CarritoService, private auth: AuthService, private productos: ProductoService) {
    this.cargar();
  }

  cargar() {
    const userId = this.auth.getUserId();
    if (userId) {
      this.carrito.get(userId).subscribe(res => { this.data = res; this.refreshCarousel(); });
    } else {
      this.carrito.getConToken().subscribe(res => { this.data = res; this.refreshCarousel(); });
    }
  }

  inc(item: any) { if (this.cargandoAccion) return; this.setCantidad(item, item.cantidad + 1); }
  dec(item: any) { if (this.cargandoAccion) return; if (item.cantidad > 1) this.setCantidad(item, item.cantidad - 1); }

  private setCantidad(item: any, cantidad: number) {
    this.cargandoAccion = true;
    this.carrito.actualizar(item.id, cantidad).subscribe({
      next: () => { this.cargar(); this.toast('Cantidad actualizada'); this.cargandoAccion = false; },
      error: (err) => { this.toast('No se pudo actualizar la cantidad'); this.cargandoAccion = false; }
    });
  }

  eliminar(item: any) {
    if (this.cargandoAccion) return;
    this.cargandoAccion = true;
    this.carrito.eliminar(item.id).subscribe({
      next: () => { this.cargar(); this.toast('Producto eliminado'); this.cargandoAccion = false; },
      error: () => { this.toast('No se pudo eliminar'); this.cargandoAccion = false; }
    });
  }

  vaciar() {
    const userId = this.auth.getUserId();
    this.cargandoAccion = true;
    const obs = userId ? this.carrito.vaciar(userId) : this.carrito.vaciarConToken();
    obs.subscribe({
      next: () => { this.cargar(); this.toast('Carrito vaciado'); this.cargandoAccion = false; },
      error: () => { this.toast('No se pudo vaciar el carrito'); this.cargandoAccion = false; }
    });
  }

  private toast(msg: string) {
    this.toastMsg = msg;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 1500);
  }

  ngOnInit() {
    this.productos.destacados().subscribe({
      next: (list) => {
        this.productosDestacados = Array.isArray(list) ? list : [];
        this.refreshCarousel();
        this.startCarouselTimer();
      },
      error: () => { /* opcional: ignorar */ }
    });
  }

  addToCart(prod: any) {
    if (!prod?.id) return;
    this.cargandoAccion = true;
    const uid = this.auth.getUserId();
    const req$ = uid ? this.carrito.agregar(uid, prod.id, 1) : this.carrito.agregarConToken(prod.id, 1);
    req$.subscribe({
      next: () => { this.cargar(); this.toast('Añadido al carrito'); this.cargandoAccion = false; },
      error: () => { this.toast('No se pudo añadir'); this.cargandoAccion = false; }
    });
  }

  private startCarouselTimer() {
    if (this.carouselTimer) clearInterval(this.carouselTimer);
    this.carouselTimer = setInterval(() => this.refreshCarousel(), 12000);
  }

  private refreshCarousel() {
    if (this.carouselFading) return; // evitar solapes durante el fade
    // Excluir productos que ya están en el carrito
    const inCartIds = new Set<number>((this.data?.items || []).map((it: any) => it?.producto?.id).filter((v: any) => v != null));
    let src = (this.productosDestacados || []).filter((p: any) => !inCartIds.has(p?.id));
    if (src.length < 3 && !this.fetchingMore) {
      this.fetchingMore = true;
      this.productos.buscar({ soloConStock: true, size: 20 }).subscribe({
        next: (resp: any) => {
          const list = Array.isArray(resp?.content) ? resp.content : (Array.isArray(resp) ? resp : []);
          // Merge únicos por id
          const byId = new Map<number, any>();
          [...(this.productosDestacados||[]), ...list].forEach((p: any) => { if (p?.id && !inCartIds.has(p.id)) byId.set(p.id, p); });
          this.productosDestacados = Array.from(byId.values());
          this.fetchingMore = false;
          // reintentar con la nueva lista
          this.refreshCarousel();
        },
        error: () => { this.fetchingMore = false; }
      });
      // mostrar lo que haya de momento
      if (!src.length) { this.carouselItems = []; }
      return;
    }
    if (!src.length) { this.carouselItems = []; return; }
    const count = Math.min(4, src.length);
    const picked: any[] = [];
    const used = new Set<number>();
    while (picked.length < count && used.size < src.length) {
      const idx = Math.floor(Math.random() * src.length);
      if (!used.has(idx)) { used.add(idx); picked.push(src[idx]); }
    }
    // Fade-out (1s), swap, then fade-in (1s)
    this.carouselFading = true;
    setTimeout(() => {
      this.carouselItems = picked;
      setTimeout(() => this.carouselFading = false, 10);
    }, 1000);
  }

  ngOnDestroy() {
    if (this.carouselTimer) clearInterval(this.carouselTimer);
  }
}
