import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminEstadisticasService } from '../../services/admin-estadisticas.service';
import { AdminNavComponent } from '../../components/admin-nav/admin-nav.component';

@Component({
  selector: 'app-admin-estadisticas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AdminNavComponent],
  templateUrl: './admin-estadisticas.component.html',
  styleUrls: ['./admin-estadisticas.component.scss']
})
export class AdminEstadisticasComponent {
  hoy = new Date();
  from = this.toISO(this.addDays(this.hoy, -6));
  to = this.toISO(this.hoy);
  groupBy: 'day' | 'week' | 'month' = 'day';

  loading = false;
  kpis: any = { ingresos: 0, pedidos: 0, reservas: 0, suscripcionesActivas: 0, moneda: 'PEN' };
  serie: Array<{ t: string; value: number }> = [];
  seriePedidos: Array<{ t: string; value: number }> = [];
  serieReservas: Array<{ t: string; value: number }> = [];
  ultimos: any[] = [];
  topsData: any = { productosIngresos: [], productosUnidades: [], planes: [], membresias: [] };
  error = '';
  deltas: { ingresos?: number; pedidos?: number; reservas?: number } = {};

  constructor(private api: AdminEstadisticasService) { this.load(); }

  toISO(d: Date) { const y=d.getFullYear(); const m=(d.getMonth()+1).toString().padStart(2,'0'); const dd=d.getDate().toString().padStart(2,'0'); return `${y}-${m}-${dd}`; }
  addDays(d: Date, n: number) { const x=new Date(d); x.setDate(x.getDate()+n); return x; }

  preset(days: number) { this.from = this.toISO(this.addDays(new Date(this.to), -Math.max(0, days-1))); this.load(); }

  load() {
    this.loading = true; this.error = '';
    // KPIs actuales
    this.api.overview({ from: this.from, to: this.to }).subscribe({
      next: o => { this.kpis = o || this.kpis; this.computeDeltas(); },
      error: () => { this.error = 'No se pudieron cargar KPIs'; }
    });
    // Series
    this.api.ingresosSeries({ from: this.from, to: this.to, groupBy: this.groupBy }).subscribe({
      next: s => { this.serie = (s||[]).map((p:any)=>({ t: p.t, value: Number(p.value||0) })); this.loading=false; },
      error: () => { this.loading=false; this.error = 'No se pudo cargar la serie de ingresos'; }
    });
    this.api.pedidosSeries({ from: this.from, to: this.to, groupBy: this.groupBy }).subscribe({
      next: s => { this.seriePedidos = (s||[]).map((p:any)=>({ t: p.t, value: Number(p.value||0) })); },
      error: () => {}
    });
    this.api.reservasSeries({ from: this.from, to: this.to, groupBy: this.groupBy }).subscribe({
      next: s => { this.serieReservas = (s||[]).map((p:any)=>({ t: p.t, value: Number(p.value||0) })); },
      error: () => {}
    });
    this.api.ultimosPedidos(8).subscribe({ next: l => this.ultimos = l || [] });
    this.api.tops({ from: this.from, to: this.to, limit: 5 }).subscribe({ next: t => this.topsData = t || this.topsData });
  }

  maxVal(): number { return Math.max(1, ...this.serie.map(p=>p.value)); }

  points(): string {
    const n = this.serie.length;
    if (n <= 1) return '';
    const max = this.maxVal();
    return this.serie.map((p, i) => {
      const x = i * (100 / (n - 1));
      const y = 40 - (p.value / max * 38);
      return `${x},${y}`;
    }).join(' ');
  }

  pointsOf(series: Array<{t:string; value:number}>): string {
    const n = series.length; if (n <= 1) return '';
    const max = Math.max(1, ...series.map(p=>p.value));
    return series.map((p, i) => {
      const x = i * (100 / (n - 1));
      const y = 40 - (p.value / max * 38);
      return `${x},${y}`;
    }).join(' ');
  }

  private computeDeltas() {
    // Rango anterior del mismo tamaño
    const fromDate = new Date(this.from);
    const toDate = new Date(this.to);
    const days = Math.max(1, Math.round((toDate.getTime() - fromDate.getTime())/86400000) + 1);
    const prevTo = this.addDays(fromDate, -1);
    const prevFrom = this.addDays(prevTo, -(days-1));
    this.api.overview({ from: this.toISO(prevFrom), to: this.toISO(prevTo) }).subscribe({
      next: prev => {
        const safe = (v:number)=> (v===0? 1 : v);
        if (prev) {
          this.deltas.ingresos = ((Number(this.kpis.ingresos||0) - Number(prev.ingresos||0)) / safe(Number(prev.ingresos||0))) * 100;
          this.deltas.pedidos = ((Number(this.kpis.pedidos||0) - Number(prev.pedidos||0)) / safe(Number(prev.pedidos||0))) * 100;
          this.deltas.reservas = ((Number(this.kpis.reservas||0) - Number(prev.reservas||0)) / safe(Number(prev.reservas||0))) * 100;
        }
      }
    });
  }

  // CSV helpers
  exportCsv(filename: string, rows: any[], headers: string[], keys: string[]) {
    const esc = (v:any) => {
      const s = (v===null||v===undefined) ? '' : String(v);
      return /[",\n]/.test(s) ? '"'+s.replace(/"/g,'""')+'"' : s;
    };
    const csv = [headers.join(',')].concat(rows.map(r => keys.map(k => esc(r[k])).join(','))).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = filename; a.click(); URL.revokeObjectURL(url);
  }

  exportSeriesCsv(kind: 'ingresos'|'pedidos'|'reservas') {
    const map: any = { ingresos: this.serie, pedidos: this.seriePedidos, reservas: this.serieReservas };
    const data = (map[kind] || []).map((p:any)=> ({ t: p.t, value: p.value }));
    this.exportCsv(`${kind}-series.csv`, data, ['t','value'], ['t','value']);
  }

  exportUltimosCsv() {
    this.exportCsv('ultimos-pedidos.csv', this.ultimos || [], ['id','usuarioId','fecha','estado','total'], ['id','usuarioId','fecha','estado','total']);
  }

  exportTopCsv(kind: 'productosIngresos'|'productosUnidades'|'planes'|'membresias') {
    const data = this.topsData?.[kind] || [];
    if (kind==='planes' || kind==='membresias') {
      this.exportCsv(`${kind}.csv`, data, ['id','nombre','cantidad'], ['id','nombre','cantidad']);
    } else {
      this.exportCsv(`${kind}.csv`, data, ['id','nombre','ingresos','unidades'], ['id','nombre','ingresos','unidades']);
    }
  }
}
