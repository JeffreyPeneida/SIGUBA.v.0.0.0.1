import {
  AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit,
  ViewChild, computed, inject, signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import * as L from 'leaflet';

import { NotificacionesService } from '../core/notificaciones.service';
import { DetalleTramite, ImagenTramite, TramitesApi } from '../core/tramites.api';

/** Icono propio: Leaflet busca los suyos en la raiz del sitio y daban 404. */
const ICONO = L.icon({
  iconUrl: 'marker-icon.png',
  iconRetinaUrl: 'marker-icon-2x.png',
  shadowUrl: 'marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  shadowSize: [41, 41]
});

/**
 * Seguimiento de una denuncia, en solo lectura.
 *
 * El ciudadano no puede entrar a /reginspeccion —es el formulario de trabajo
 * del tecnico y exige ese rol—, asi que hasta ahora el boton "ver detalle" de
 * "Mis denuncias" no llevaba a ninguna parte. Esta pantalla responde lo unico
 * que el denunciante viene a preguntar: en que va lo suyo.
 */
@Component({
  selector: 'app-detalle-denuncia',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './detalle-denuncia.html'
})
export class DetalleDenunciaComponent implements OnInit, AfterViewInit {

  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(TramitesApi);
  private readonly aviso = inject(NotificacionesService);
  private readonly cd = inject(ChangeDetectorRef);

  @ViewChild('mapa') contenedorMapa?: ElementRef<HTMLDivElement>;
  private mapa?: L.Map;

  readonly detalle = signal<DetalleTramite | null>(null);
  readonly imagenes = signal<ImagenTramite[]>([]);
  readonly cargando = signal(true);
  readonly imagenAmpliada = signal<string | null>(null);

  /** Las tres etapas por las que pasa una denuncia. */
  readonly etapas = computed(() => {

    const d = this.detalle();

    const registrada = !!d;
    const asignada = !!d?.id_tecnico;
    const inspeccionada = !!d?.fecha_inspeccion;

    return [
      { texto: 'Registrada',   icono: 'fa-file-circle-plus',  hecha: registrada,
        detalle: d?.fecha_denuncia ?? null },
      { texto: 'Técnico asignado', icono: 'fa-user-check',    hecha: asignada,
        detalle: d?.tecnico ?? 'Pendiente de asignación' },
      { texto: 'Inspeccionada', icono: 'fa-clipboard-check',  hecha: inspeccionada,
        detalle: d?.fecha_inspeccion ?? 'Pendiente de inspección' }
    ];
  });

  ngOnInit(): void {

    const id = Number(this.ruta.snapshot.paramMap.get('id'));

    if (!id) {
      this.cargando.set(false);
      return;
    }

    this.api.detalle(id).subscribe({
      next: d => {
        this.detalle.set(d);
        this.cargando.set(false);
        this.cd.markForCheck();
        setTimeout(() => this.pintarMapa());
      },
      error: e => {
        this.cargando.set(false);
        this.aviso.errorHttp(e, 'No se pudo cargar la denuncia.');
        this.cd.markForCheck();
      }
    });

    this.api.imagenesDenuncia(id).subscribe({
      next: i => { this.imagenes.set(i ?? []); this.cd.markForCheck(); },
      error: () => this.imagenes.set([])
    });
  }

  ngAfterViewInit(): void {
    this.pintarMapa();
  }

  /** Mapa de consulta: ubica, no se navega ni se edita. */
  private pintarMapa(): void {

    const d = this.detalle();
    const lat = Number(d?.latitud);
    const lon = Number(d?.longitud);

    if (!this.contenedorMapa || this.mapa
        || !isFinite(lat) || !isFinite(lon) || (lat === 0 && lon === 0)) {
      return;
    }

    this.mapa = L.map(this.contenedorMapa.nativeElement, {
      center: [lat, lon],
      zoom: 16,
      scrollWheelZoom: false,
      attributionControl: false
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 })
      .addTo(this.mapa);

    L.marker([lat, lon], { icon: ICONO }).addTo(this.mapa);

    // El contenedor puede haber cambiado de alto al llegar los datos.
    setTimeout(() => this.mapa?.invalidateSize(), 200);
  }

  get tieneCoordenadas(): boolean {
    const d = this.detalle();
    return !!d?.latitud && !!d?.longitud;
  }

  volver(): void {
    // history.back() devolveria a la pantalla de la que se vino, que puede ser
    // "Mis denuncias" o el panel de control segun el rol.
    history.back();
  }
}
