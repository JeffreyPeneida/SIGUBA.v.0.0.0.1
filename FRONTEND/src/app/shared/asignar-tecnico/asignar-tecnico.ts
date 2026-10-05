import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NotificacionesService } from '../../core/notificaciones.service';
import { Tramite, TecnicoCarga, TramitesApi } from '../../core/tramites.api';

/**
 * Asignacion de un tecnico a un tramite.
 *
 * Antes esto navegaba a la pantalla de tecnicos con el identificador en la URL
 * y esa pantalla lo ignoraba: no asignaba nada. Ahora se resuelve donde estas,
 * porque asignar es una decision de un clic y no merece cambiar de contexto.
 *
 * La lista muestra la carga de cada tecnico y viene ordenada por quien tiene
 * menos pendientes: repartir a ciegas es como estaba antes.
 */
@Component({
  selector: 'app-asignar-tecnico',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './asignar-tecnico.html'
})
export class AsignarTecnicoComponent {

  private readonly tramitesApi = inject(TramitesApi);
  private readonly aviso = inject(NotificacionesService);

  /** El tramite a asignar; null cierra el dialogo. */
  readonly tramite = input<Tramite | null>(null);

  readonly cerrado = output<void>();
  readonly asignado = output<Tramite>();

  readonly tecnicos = signal<TecnicoCarga[]>([]);
  readonly cargando = signal(false);
  readonly guardando = signal<number | null>(null);
  readonly filtro = signal('');

  /** Identificador del tecnico que ya tiene el tramite, si lo tiene. */
  readonly actual = computed(() => this.tramite()?.tecnico?.idTecnico ?? null);

  readonly visibles = computed<TecnicoCarga[]>(() => {
    const texto = this.filtro().trim().toLowerCase();
    if (!texto) return this.tecnicos();

    return this.tecnicos().filter(t =>
      [t.nombre, t.apellido, t.cedula, t.correo]
        .some(c => (c ?? '').toLowerCase().includes(texto)));
  });

  constructor() {
    // La carga se relee cada vez que se abre el dialogo, no una sola vez al
    // construirlo: el componente vive siempre en el arbol, asi que tras una
    // asignacion los contadores se quedaban con el valor anterior.
    effect(() => {
      if (this.tramite()) this.cargar();
    });
  }

  cargar(): void {

    this.cargando.set(true);

    this.tramitesApi.tecnicosDisponibles().subscribe({
      next: l => { this.tecnicos.set(l ?? []); this.cargando.set(false); },
      error: e => {
        this.cargando.set(false);
        this.aviso.errorHttp(e, 'No se pudo cargar el personal técnico.');
      }
    });
  }

  /** Semáforo de carga: verde libre, ámbar ocupado, rojo saturado. */
  colorCarga(pendientes: number): string {
    if (pendientes === 0) return 'bg-emerald-50 text-emerald-700 ring-emerald-200';
    if (pendientes <= 3)  return 'bg-amber-50 text-amber-700 ring-amber-200';
    return 'bg-red-50 text-red-700 ring-red-200';
  }

  textoCarga(pendientes: number): string {
    if (pendientes === 0) return 'Sin pendientes';
    return `${pendientes} pendiente${pendientes === 1 ? '' : 's'}`;
  }

  iniciales(t: TecnicoCarga): string {
    return `${t.nombre?.[0] ?? ''}${t.apellido?.[0] ?? ''}`.toUpperCase();
  }

  async asignar(tecnico: TecnicoCarga): Promise<void> {

    const t = this.tramite();
    if (!t) return;

    // Reasignar es distinto de asignar: se avisa de que hay alguien detras.
    if (this.actual() !== null && this.actual() !== tecnico.idTecnico) {

      const anterior = `${t.tecnico?.nombre ?? ''} ${t.tecnico?.apellido ?? ''}`.trim();

      const ok = await this.aviso.confirmar(
        'Reasignar el trámite',
        `${anterior || 'El técnico actual'} dejará de tener este trámite y pasará `
        + `a ${tecnico.nombre} ${tecnico.apellido}.`,
        'Reasignar');

      if (!ok) return;
    }

    this.guardando.set(tecnico.idTecnico);

    this.tramitesApi.asignarTecnico(t.idCodigoInterno, tecnico.idTecnico).subscribe({

      next: () => {
        this.guardando.set(null);
        this.aviso.exito(
          `${t.idTramite ?? 'El trámite'} quedó asignado a ${tecnico.nombre} ${tecnico.apellido}.`);
        this.asignado.emit(t);
        this.cerrado.emit();
      },

      error: e => {
        this.guardando.set(null);
        this.aviso.errorHttp(e, 'No se pudo asignar el técnico.');
      }
    });
  }
}
