import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import jsQR from 'jsqr';
import { TrabajadorService } from '../../services/trabajador.service';
import { TrabajadorNavComponent } from '../../components/trabajador-nav/trabajador-nav.component';

@Component({
  selector: 'app-trabajador-checkin',
  standalone: true,
  imports: [CommonModule, RouterModule, TrabajadorNavComponent],
  templateUrl: './trabajador-checkin.component.html',
  styleUrls: ['./trabajador-checkin.component.scss']
})
export class TrabajadorCheckinComponent implements AfterViewInit, OnDestroy {
  @ViewChild('video') videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  cargandoCamara = false;
  error: string | null = null;
  resultado: any = null;
  scanning = false;
  private stream: MediaStream | null = null;
  private clearTimeoutId: any = null;
  usuariosResumen: any[] = [];

  constructor(private ws: TrabajadorService) {}

  ngAfterViewInit(): void {
    this.iniciarEscaneo();
    this.cargarResumenUsuarios();
  }

  ngOnDestroy(): void {
    this.detenerCamara();
  }

  private async iniciarEscaneo() {
    this.error = null;
    this.resultado = null;

    if (!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia)) {
      this.error = 'Tu navegador no permite usar la cámara.';
      return;
    }

    this.cargandoCamara = true;
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      const video = this.videoRef.nativeElement;
      video.srcObject = this.stream;
      await video.play();
      this.cargandoCamara = false;
      this.scanning = true;
      this.loopScan();
    } catch (e) {
      this.cargandoCamara = false;
      this.error = 'No se pudo acceder a la cámara.';
    }
  }

  private detenerCamara() {
    if (this.stream) {
      this.stream.getTracks().forEach(t => t.stop());
      this.stream = null;
    }
    this.scanning = false;
    if (this.clearTimeoutId) {
      clearTimeout(this.clearTimeoutId);
      this.clearTimeoutId = null;
    }
  }

  private async loopScan() {
    if (!this.scanning) return;

    const video = this.videoRef.nativeElement;
    const canvas = this.canvasRef.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx || video.readyState !== video.HAVE_ENOUGH_DATA) {
      requestAnimationFrame(() => this.loopScan());
      return;
    }

    try {
      canvas.width = video.videoWidth || 640;
      canvas.height = video.videoHeight || 480;
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const code = jsQR(imageData.data, canvas.width, canvas.height);

      if (code && code.data) {
        const value: string = code.data || '';
        if (value) {
          this.scanning = false;
          this.procesarToken(value);
          return;
        }
      }
    } catch {
      // si falla la detección, continuamos intentando
    }

    requestAnimationFrame(() => this.loopScan());
  }

  private procesarToken(token: string) {
    this.error = null;
    this.resultado = null;
    if (this.clearTimeoutId) {
      clearTimeout(this.clearTimeoutId);
      this.clearTimeoutId = null;
    }
    this.ws.registrarCheckinPorToken(token).subscribe({
      next: (res) => {
        this.resultado = res;
        this.cargarResumenUsuarios();
        this.clearTimeoutId = setTimeout(() => {
          if (!this.stream) {
            return;
          }
          this.resultado = null;
          this.error = null;
          this.scanning = true;
          this.loopScan();
        }, 10000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'No se pudo registrar el check-in.';
      }
    });
  }

  private cargarResumenUsuarios() {
    this.ws.resumenAsistenciaUsuarios().subscribe({
      next: (list) => {
        this.usuariosResumen = list || [];
      },
      error: () => {
        // si falla, mantenemos la lista actual
      }
    });
  }

  reintentar() {
    this.detenerCamara();
    this.iniciarEscaneo();
  }

  cancelarEscaneo() {
    this.detenerCamara();
  }
}
