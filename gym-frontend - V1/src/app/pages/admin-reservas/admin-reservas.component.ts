import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminReservasService } from '../../services/admin-reservas.service';
import { AdminNavComponent } from '../../components/admin-nav/admin-nav.component';

@Component({
  selector: 'app-admin-reservas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, AdminNavComponent],
  templateUrl: './admin-reservas.component.html',
  styleUrls: ['./admin-reservas.component.scss']
})
export class AdminReservasComponent {
  hoy = new Date();
  desde = this.toIsoDate(this.hoy);
  cargando = false;
  slots: any[] = [];

  modal = false;
  sel: any = null;

  constructor(private api: AdminReservasService) { this.load(); }

  toIsoDate(d: Date) {
    const y = d.getFullYear();
    const m = (d.getMonth()+1).toString().padStart(2,'0');
    const day = d.getDate().toString().padStart(2,'0');
    return `${y}-${m}-${day}T00:00:00`;
  }

  moveDays(n: number) {
    const d = new Date(this.desde);
    d.setDate(d.getDate()+n);
    this.desde = this.toIsoDate(d);
    this.load();
  }

  load() {
    this.cargando = true;
    this.api.semana({ desde: this.desde })
      .subscribe({ next: res => { this.slots = (res||[]).map((r:any)=>({ ...r, fechaObj: new Date(r.fecha) })); this.cargando=false; }, error:()=> this.cargando=false });
  }

  hours(): number[] {
    // Mostrar siempre el rango fijo 06:00–22:00 (inclusive)
    return Array.from({ length: 17 }, (_, i) => i + 6);
  }
  days(): Date[] {
    const start = new Date(this.desde);
    return Array.from({length:7}, (_,i)=> new Date(start.getFullYear(), start.getMonth(), start.getDate()+i));
  }
  dayLabel(d: Date) {
    return d.toLocaleDateString(undefined, { weekday:'short', month:'short', day:'numeric' });
  }

  cellSlots(day: Date, hour: number) {
    const s = new Date(day.getFullYear(), day.getMonth(), day.getDate(), hour, 0, 0);
    const e = new Date(s.getTime()+60*60*1000);
    return this.slots.filter(r => {
      const rf = r.fechaObj as Date;
      const rEnd = new Date(rf.getTime() + (r.duracionMinutos||60)*60000);
      return !(rEnd <= s || rf >= e);
    });
  }

  open(r: any) { this.sel = r; this.modal = true; }

  allByDate(): any[] {
    return [...this.slots].sort((a,b)=> (new Date(a.fecha)).getTime() - (new Date(b.fecha)).getTime());
  }
}
