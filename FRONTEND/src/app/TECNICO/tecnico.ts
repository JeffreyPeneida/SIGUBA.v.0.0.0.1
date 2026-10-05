import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { NotificacionesService } from '../core/notificaciones.service';
import { Tramite, TramitesApi } from '../core/tramites.api';
import { TramitesTablaComponent } from '../shared/tramites-tabla/tramites-tabla';

type Etapa = 'porInspeccionar' | 'inspeccionados';

/**
 * Bandeja de trabajo del tecnico.
 *
 * Antes era una maqueta con paneles vacios. Ahora muestra los tramites que
 * tiene asignados, separados por lo unico que importa en campo: los que faltan
 * por inspeccionar y los ya cerrados.
 */
@Component({
  selector: 'app-tecnico',
  standalone: true,
  imports: [CommonModule, TramitesTablaComponent],
  templateUrl: './tecnico.html'
})
export class TecnicoComponent implements OnInit {

  private readonly auth = inject(AuthService);
  private readonly tramitesApi = inject(TramitesApi);
  private readonly aviso = inject(NotificacionesService);
  private readonly router = inject(Router);

  readonly usuario = this.auth.usuario;

  readonly tramites = signal<Tramite[]>([]);
  readonly cargando = signal(false);
  readonly etapa = signal<Etapa>('porInspeccionar');
  readonly filtro = signal('');

  /** La administración ve la carga de todos; el técnico, solo la suya. */
  readonly esAdmin = computed(() => this.auth.tieneRol('ADMIN'));

  readonly visibles = computed<Tramite[]>(() => {

    const texto = this.filtro().trim().toLowerCase();

    return this.tramites().filter(t => {

      const cerrado = !!t.fechaInspeccion;

      if (this.etapa() === 'porInspeccionar' && cerrado) return false;
      if (this.etapa() === 'inspeccionados' && !cerrado) return false;

      if (!texto) return true;

      const denunciante = `${t.denunciante?.nombre ?? ''} ${t.denunciante?.apellido ?? ''}`;

      return [t.idTramite, denunciante, t.ubicacion?.direccion, t.barrio?.nombre]
        .some(c => (c ?? '').toLowerCase().includes(texto));
    });
  });

  readonly conteo = computed(() => {
    const t = this.tramites();
    return {
      porInspeccionar: t.filter(x => !x.fechaInspeccion).length,
      inspeccionados:  t.filter(x => x.fechaInspeccion).length
    };
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {

    this.cargando.set(true);

    // El backend resuelve que tecnico es quien pregunta a partir del token.
    // Antes se traian todos los tramites asignados y se filtraban aqui, con lo
    // que cada tecnico veia tambien el trabajo de los demas.
    this.tramitesApi.misAsignados().subscribe({
      next: lista => {
        this.tramites.set(lista ?? []);
        this.cargando.set(false);
      },
      error: e => {
        this.cargando.set(false);
        this.aviso.errorHttp(e, 'No se pudieron cargar tus trámites.');
      }
    });
  }

  inspeccionar(t: Tramite): void {
    this.router.navigate(['/reginspeccion'], {
      queryParams: { tramite: t.idCodigoInterno }
    });
  }

  verDetalle(t: Tramite): void {
    this.inspeccionar(t);
  }

  abrirInforme(t: Tramite): void {
    this.router.navigate(['/informe-inspeccion', t.idCodigoInterno]);
  }
}
