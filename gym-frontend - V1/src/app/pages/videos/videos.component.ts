import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SuscripcionesService } from '../../services/suscripciones.service';
import { PerfilService } from '../../services/perfil.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

interface VideoSection {
  key: string;
  titulo: string;
  beneficio: string;
  descripcion: string;
  plan: string;
  enabled?: boolean;
  open?: boolean;
  videos: { url: string; id?: string; thumb?: string; thumbMax?: string; embed?: SafeResourceUrl; }[];
}

@Component({
  selector: 'app-videos',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './videos.component.html',
  styleUrls: ['./videos.component.scss']
})
export class VideosComponent implements OnDestroy {
  secciones: VideoSection[] = [];
  tieneAcceso = false;
  activos: string[] = [];
  trialActivo = false;
  codigos: string[] = [];
  modalOpen = false;
  modalSrc?: SafeResourceUrl;
  private rafs = new Map<HTMLElement, number>();
  private speeds = new Map<HTMLElement, number>(); // px per second

  constructor(private auth: AuthService, private sus: SuscripcionesService, private router: Router, private sanitizer: DomSanitizer, private perfil: PerfilService) {
    if (!this.auth.isLoggedIn()) { this.router.navigate(['/login']); return; }
    const loadFromPerfil = () => this.perfil.planesMe().subscribe({
      next: (list) => {
        const arr = Array.isArray(list) ? list : [];
        const ahora = Date.now();
        const enVigencia = (s: any) => {
          try {
            const ini = s?.fechaInicio ? new Date(s.fechaInicio).getTime() : undefined;
            const fin = s?.fechaFin ? new Date(s.fechaFin).getTime() : undefined;
            if (ini && ahora < ini) return false;
            if (fin && ahora > fin) return false;
            return true;
          } catch { return true; }
        };
        const activas = arr.filter((s: any) => (s?.estado || '').toString().toUpperCase() === 'ACTIVA' && enVigencia(s));
        this.tieneAcceso = activas.length > 0;
        // nombres posibles
        const nombreDe = (s: any) => (
          s?.membresiaNombre || s?.membresia?.nombre || s?.planNombre || s?.nombre || s?.planSuscripcion?.membresia?.nombre || ''
        ).toString();
        this.activos = activas.map(nombreDe);
        // códigos posibles (M1/M2/M3)
        const codeOf = (s: any): string => (
          s?.codigo || s?.membresia?.codigo || s?.planSuscripcion?.membresia?.codigo || ''
        ).toString().toUpperCase();
        const codesFromFields = activas.map(codeOf).filter(Boolean);
        const codesFromNames = this.activos.map(n => {
          const m = /\((M\d)\)/i.exec(n);
          return m ? m[1].toUpperCase() : '';
        }).filter(Boolean);
        this.codigos = Array.from(new Set([ ...codesFromFields, ...codesFromNames ]));
        this.trialActivo = this.activos.some(n => this.tiene(n, 'trial') || this.tiene(n, 'prueba'));
        this.cargarSecciones();
      },
      error: () => {
        // Fallback a servicio de suscripciones si perfil falla
        this.sus.mias().subscribe(list => {
          const arr = Array.isArray(list) ? list : [];
          const activas = arr.filter((s: any) => {
            const est = (s?.estado || '').toString().toUpperCase();
            return est === 'ACTIVA' || est === 'ACTIVO';
          });
          this.tieneAcceso = activas.length > 0;
          this.activos = activas.map((s: any) => (s?.membresiaNombre || s?.planNombre || s?.nombre || '').toString());
          const codesFromFields = activas.map((s: any) => (s?.codigo || '').toString().toUpperCase()).filter(Boolean);
          const codesFromNames = this.activos.map(n => {
            const m = /\((M\d)\)/i.exec(n);
            return m ? m[1].toUpperCase() : '';
          }).filter(Boolean);
          this.codigos = Array.from(new Set([ ...codesFromFields, ...codesFromNames ]));
          this.trialActivo = this.activos.some(n => this.tiene(n, 'trial') || this.tiene(n, 'prueba'));
          this.cargarSecciones();
        });
      }
    });
    loadFromPerfil();
  }

  private tiene(nombre: string, term: string): boolean {
    return nombre.toLowerCase().includes(term.toLowerCase());
  }

  private cargarSecciones() {
    const enable = (term: string) => this.activos.some(n => this.tiene(n, term));
    const hasCode = (code: string) => this.codigos.includes(code.toUpperCase());

    const defs: VideoSection[] = [
      { key: 'hiit', titulo: 'HIIT', beneficio: 'Vídeos HIIT', descripcion: 'Acceso a videos HIIT (Entrenamiento de Intervalos de Alta Intensidad).', plan: 'Cardio Total (M1)', videos: [
        { url: 'https://www.youtube.com/watch?v=E937HCqu9O0' },
        { url: 'https://www.youtube.com/watch?v=Pv6NrM7fqHY' },
        { url: 'https://www.youtube.com/watch?v=0Grvq1Kz6L8' },
      ] },
      { key: 'fuerza', titulo: 'Fuerza', beneficio: 'Vídeos de fuerza', descripcion: 'Rutinas de fuerza.', plan: 'Fuerza Plus (M2)', videos: [
        { url: 'https://www.youtube.com/watch?v=0BjHtT1fgA0' },
        { url: 'https://www.youtube.com/watch?v=NSZkM9s9WWI' },
        { url: 'https://www.youtube.com/watch?v=WLw0FGmO9sI' },
      ] },
      { key: 'movilidad', titulo: 'Movilidad y Estiramientos', beneficio: 'Vídeos movilidad', descripcion: 'Movilidad y estiramientos.', plan: 'Movilidad (M3)', videos: [
        { url: 'https://www.youtube.com/watch?v=R8f9LWDQVW4' },
        { url: 'https://www.youtube.com/watch?v=5KG-LBgBw3g' },
        { url: 'https://www.youtube.com/watch?v=q642sAmp9FM' },
      ] },
      { key: 'adelgazamiento', titulo: 'Adelgazamiento / Cardio Total', beneficio: 'Reduccion de grasa', descripcion: 'Acceso a videos de adelgazamiento.', plan: 'Cardio Total', videos: [
        { url: 'https://www.youtube.com/watch?v=w5ooW13AmBY' },
        { url: 'https://www.youtube.com/watch?v=uC78zb63dkc' },
      ] },
    ];

    this.secciones = defs.map(def => {
      let ok = this.trialActivo; // Trial 7 días ve todo
      if (def.key === 'hiit' || def.key === 'adelgazamiento') ok = ok || enable('cardio total') || hasCode('M1');
      if (def.key === 'fuerza') ok = ok || enable('fuerza plus') || hasCode('M2');
      if (def.key === 'movilidad') ok = ok || enable('movilidad') || hasCode('M3');
      // map to id, thumb and embed url
      const mapped = (def.videos || []).map(v => {
        const id = this.getYouTubeId(v.url);
        return { url: v.url, id, thumb: id ? this.toThumb(id) : undefined, thumbMax: id ? this.toThumbMax(id) : undefined, embed: this.toEmbedFromId(id) };
      });
      return { ...def, enabled: ok, open: false, videos: mapped };
    });
  }

  private getYouTubeId(url: string): string | undefined {
    try {
      const u = new URL(url);
      if (u.hostname.includes('youtube.com')) {
        if (u.pathname === '/watch') return u.searchParams.get('v') || undefined;
        if (u.pathname.startsWith('/shorts/')) return u.pathname.split('/')[2] || undefined;
        if (u.pathname.startsWith('/embed/')) return u.pathname.split('/')[2] || undefined;
      } else if (u.hostname === 'youtu.be') {
        return (u.pathname.split('/')[1] || '').trim() || undefined;
      }
      return undefined;
    } catch { return undefined; }
  }

  private toThumb(id: string): string {
    return `https://img.youtube.com/vi/${id}/hqdefault.jpg`;
  }

  private toThumbMax(id: string): string {
    return `https://img.youtube.com/vi/${id}/maxresdefault.jpg`;
  }

  private toEmbedFromId(id?: string): SafeResourceUrl | undefined {
    if (!id) return undefined;
    return this.sanitizer.bypassSecurityTrustResourceUrl(`https://www.youtube.com/embed/${id}`);
  }

  private toEmbed(url: string): SafeResourceUrl { // kept for fallback
    try {
      // Support typical YouTube formats
      const u = new URL(url);
      let id = '';
      if (u.hostname.includes('youtube.com')) {
        if (u.pathname === '/watch') id = u.searchParams.get('v') || '';
        if (u.pathname.startsWith('/shorts/')) id = u.pathname.split('/')[2] || '';
        if (u.pathname.startsWith('/embed/')) id = u.pathname.split('/')[2] || '';
      } else if (u.hostname === 'youtu.be') {
        id = (u.pathname.split('/')[1] || '').trim();
      }
      const embed = id ? `https://www.youtube.com/embed/${id}` : url;
      return this.sanitizer.bypassSecurityTrustResourceUrl(embed);
    } catch {
      return this.sanitizer.bypassSecurityTrustResourceUrl(url);
    }
  }

  openModal(src: SafeResourceUrl | undefined) {
    if (!src) return;
    this.modalSrc = src;
    this.modalOpen = true;
  }

  closeModal() {
    this.modalOpen = false;
    this.modalSrc = undefined;
  }

  scrollRow(el: HTMLElement | null | undefined, dir: number) {
    try {
      if (!el) return;
      const first = el.querySelector('.video') as HTMLElement | null;
      const style = getComputedStyle(el);
      const gapStr = (style as any).gap || (style as any).columnGap || '0';
      const gap = parseFloat(gapStr) || 0;
      const cardW = (first?.offsetWidth || Math.min(420, el.clientWidth)) + gap;
      const amount = cardW * (dir > 0 ? 1 : -1);
      el.scrollBy({ left: amount, behavior: 'smooth' });
    } catch { /* ignore */ }
  }

  openFirst(section: VideoSection) {
    if (!section?.videos?.length) return;
    const first = section.videos[0];
    this.openModal(first.embed);
  }

  toggleOpen(section: VideoSection) {
    if (!section.enabled) return;
    section.open = !section.open;
    // Auto-scroll control: start when open, stop when close
    setTimeout(() => {
      const el = document.getElementById(`row-${section.key}`) as HTMLElement | null;
      if (!el) return;
      if (section.open) {
        this.startAutoScroll(el);
      } else {
        this.stopAutoScroll(el);
      }
    }, 0);
  }

  // drag-to-scroll deshabilitado: usamos flechas para navegar por tarjeta

  // Auto-scroll helpers
  private startAutoScroll(el: HTMLElement) {
    this.stopAutoScroll(el);
    const max = () => el.scrollWidth - el.clientWidth;
    if (max() <= 0) return;
    const speed = this.speeds.get(el) ?? 60; // px/s
    this.speeds.set(el, speed);
    let last = performance.now();
    const loop = (t: number) => {
      const dt = (t - last) / 1000;
      last = t;
      const m = max();
      if (m > 0) {
        let next = el.scrollLeft + speed * dt;
        if (next >= m) next = 0; // loop to start
        el.scrollLeft = next;
      }
      const id = requestAnimationFrame(loop);
      this.rafs.set(el, id);
    };
    const id = requestAnimationFrame(loop);
    this.rafs.set(el, id);
  }

  private stopAutoScroll(el: HTMLElement) {
    const id = this.rafs.get(el);
    if (id) { cancelAnimationFrame(id); this.rafs.delete(el); }
  }

  pauseAuto(el: HTMLElement) { this.stopAutoScroll(el); }
  resumeAuto(el: HTMLElement) { this.startAutoScroll(el); }

  ngOnDestroy(): void {
    this.rafs.forEach((id) => cancelAnimationFrame(id));
    this.rafs.clear();
  }
}
