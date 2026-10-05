import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { NotificacionesService } from '../core/notificaciones.service';
import { PermisoPantalla, PermisosService } from '../core/permisos.service';

/** Iconos ofrecidos al elegir: los del sistema y algunos de uso habitual. */
const ICONOS = [
  'fa-house', 'fa-gauge-high', 'fa-chart-line', 'fa-chart-pie', 'fa-file-circle-plus', 'fa-file-lines',
  'fa-folder-open', 'fa-folder', 'fa-inbox', 'fa-bug', 'fa-spider', 'fa-shield-dog', 'fa-helmet-safety',
  'fa-clipboard-check', 'fa-clipboard-list', 'fa-list-check', 'fa-map-location-dot', 'fa-map', 'fa-location-dot',
  'fa-users', 'fa-user', 'fa-user-gear', 'fa-user-shield', 'fa-user-tag', 'fa-id-card', 'fa-layer-group',
  'fa-table-list', 'fa-sitemap', 'fa-gear', 'fa-sliders', 'fa-lock', 'fa-bell', 'fa-calendar-days',
  'fa-camera', 'fa-flask', 'fa-building', 'fa-city', 'fa-truck', 'fa-screwdriver-wrench', 'fa-circle-info'
];

/**
 * Editor del menu lateral: nombre, icono, seccion, orden y visibilidad de cada
 * pantalla. Lo que se ve a la derecha es el menu tal como quedara.
 *
 * Que roles ven cada pantalla se decide en la otra pestana (permisos); aqui
 * solo se decide como aparece. "Oculta del menu" sigue abriendose por su ruta
 * para quien tenga permiso; "Desactivada" no la abre nadie.
 */
@Component({
  selector: 'app-menu-lateral-editor',
  standalone: true,
  templateUrl: './menu-lateral.html'
})
export class MenuLateralEditorComponent implements OnInit {

  private readonly permisos = inject(PermisosService);
  private readonly aviso = inject(NotificacionesService);

  readonly iconos = ICONOS;
  readonly puedeEditar = computed(() => this.permisos.puede('perfiles', 'EDITAR'));

  readonly cargando = signal(false);
  readonly guardando = signal(false);

  private readonly original = signal<PermisoPantalla[]>([]);
  readonly filas = signal<PermisoPantalla[]>([]);

  /** Clave de la pantalla cuyo selector de iconos esta abierto. */
  readonly eligiendoIcono = signal<string | null>(null);

  readonly secciones = computed(() => [...new Set(this.filas().map(f => f.seccion.trim()).filter(Boolean))]);

  readonly hayCambios = computed(() =>
    JSON.stringify(this.normalizar(this.filas())) !== JSON.stringify(this.normalizar(this.original())));

  readonly errores = computed(() =>
    this.filas().filter(f => !f.nombre.trim() || !f.seccion.trim()).map(f => f.clave));

  /** El menu como quedara: solo lo activo y visible, agrupado por seccion en su orden. */
  readonly vistaPrevia = computed(() => {
    const grupos = new Map<string, PermisoPantalla[]>();
    for (const f of this.filas()) {
      if (!f.activa || !f.enMenu) continue;
      const s = f.seccion.trim() || 'Sin sección';
      grupos.set(s, [...(grupos.get(s) ?? []), f]);
    }
    return [...grupos].map(([titulo, pantallas]) => ({ titulo, pantallas }));
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.permisos.pantallasMenu().subscribe({
      next: p => { this.aplicar(p ?? []); this.cargando.set(false); },
      error: e => {
        this.cargando.set(false);
        this.aviso.errorHttp(e, 'No se pudo cargar el menú.');
      }
    });
  }

  /** Perfiles no se puede ocultar ni desactivar: es desde donde se deshace el cambio. */
  protegida(f: PermisoPantalla): boolean {
    return f.clave === 'perfiles';
  }

  cambiar(clave: string, campo: 'nombre' | 'icono' | 'seccion', valor: string): void {
    this.filas.update(l => l.map(f => f.clave === clave ? { ...f, [campo]: valor } : f));
  }

  alternar(f: PermisoPantalla, campo: 'enMenu' | 'activa'): void {
    if (!this.puedeEditar() || this.protegida(f)) return;
    this.filas.update(l => l.map(x => x.clave === f.clave ? { ...x, [campo]: !x[campo] } : x));
  }

  elegirIcono(clave: string, icono: string): void {
    this.cambiar(clave, 'icono', icono);
    this.eligiendoIcono.set(null);
  }

  mover(indice: number, delta: number): void {
    const destino = indice + delta;
    this.filas.update(l => {
      if (destino < 0 || destino >= l.length) return l;
      const copia = [...l];
      [copia[indice], copia[destino]] = [copia[destino], copia[indice]];
      return copia;
    });
  }

  descartar(): void {
    this.filas.set(structuredClone(this.original()));
    this.eligiendoIcono.set(null);
  }

  guardar(): void {

    if (this.errores().length) {
      this.aviso.aviso('Cada pantalla necesita nombre y sección.');
      return;
    }

    this.guardando.set(true);

    this.permisos.guardarPantallas(this.normalizar(this.filas())).subscribe({
      next: p => {
        this.guardando.set(false);
        this.aplicar(p ?? []);
        this.aviso.exito('Menú lateral actualizado.');
        // El menu de quien edita cambia en el momento.
        this.permisos.recargar().subscribe();
      },
      error: e => {
        this.guardando.set(false);
        this.aviso.errorHttp(e, 'No se pudo guardar el menú.');
      }
    });
  }

  /** El orden es la posicion en la lista, de 10 en 10 para dejar hueco. */
  private normalizar(filas: PermisoPantalla[]) {
    return filas.map((f, i) => ({
      clave: f.clave,
      nombre: f.nombre.trim(),
      icono: f.icono,
      seccion: f.seccion.trim(),
      orden: (i + 1) * 10,
      enMenu: f.enMenu,
      activa: f.activa
    }));
  }

  private aplicar(p: PermisoPantalla[]): void {
    this.original.set(structuredClone(p));
    this.filas.set(structuredClone(p));
    this.eligiendoIcono.set(null);
  }
}
