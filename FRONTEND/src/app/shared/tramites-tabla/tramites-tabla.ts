import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Tramite } from '../../core/tramites.api';

/**
 * Tabla de tramites reutilizable.
 *
 * Las mismas columnas aparecian copiadas en control-plagas, tecnico y el panel
 * de inspecciones; cualquier cambio habia que hacerlo tres veces. Las acciones
 * se declaran desde fuera para que cada pantalla muestre solo las suyas.
 */
@Component({
  selector: 'app-tramites-tabla',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tramites-tabla.html'
})
export class TramitesTablaComponent {

  readonly tramites = input.required<Tramite[]>();
  readonly cargando = input(false);
  readonly vacioTitulo = input('No hay trámites');
  readonly vacioTexto = input('Cuando se registren aparecerán aquí.');

  /** Columnas opcionales: cada pantalla enciende las que le interesan. */
  readonly mostrarTecnico = input(true);
  readonly mostrarEstado = input(true);

  /** Acciones: si no hay quien las escuche, el boton no se dibuja. */
  readonly verDetalle = output<Tramite>();
  readonly asignar = output<Tramite>();
  readonly inspeccionar = output<Tramite>();
  readonly anular = output<Tramite>();
  readonly borrar = output<Tramite>();
  readonly informe = output<Tramite>();

  readonly puedeAsignar = input(false);
  readonly puedeInspeccionar = input(false);

  /** Informe tecnico (Word/PDF): tiene sentido una vez inspeccionado o en curso. */
  readonly puedeInforme = input(false);

  /** Solo la administracion depura datos. */
  readonly puedeBorrar = input(false);

  nombreDenunciante(t: Tramite): string {
    const d = t.denunciante;
    if (!d) return 'Sin denunciante';
    return `${d.nombre ?? ''} ${d.apellido ?? ''}`.trim() || 'Sin nombre';
  }

  nombreTecnico(t: Tramite): string {
    const x = t.tecnico;
    if (!x) return '—';
    return `${x.nombre ?? ''} ${x.apellido ?? ''}`.trim() || '—';
  }

  /** Etapa del tramite, deducida de los datos que ya trae. */
  etapa(t: Tramite): { texto: string; clase: string } {

    if ((t.estado ?? '').toUpperCase() === 'INACTIVO') {
      return { texto: 'Anulado', clase: 'bg-slate-100 text-slate-500 ring-slate-200' };
    }

    if (t.fechaInspeccion) {
      return { texto: 'Inspeccionado', clase: 'bg-emerald-50 text-emerald-700 ring-emerald-200' };
    }

    if (t.tecnico) {
      return { texto: 'Asignado', clase: 'bg-sky-50 text-sky-700 ring-sky-200' };
    }

    return { texto: 'Pendiente', clase: 'bg-amber-50 text-amber-700 ring-amber-200' };
  }
}
