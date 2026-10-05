import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';

/** Por debajo de este ancho el menu deja de ser fijo y pasa a ser un cajon. */
const ANCHO_CAJON = '(max-width: 1279.98px)';

/**
 * Estado del menu lateral.
 *
 * En pantallas anchas el menu es fijo y siempre visible. Por debajo de 1280px
 * se convierte en un cajon: lo abre el boton del header y lo cierran el velo,
 * la tecla Escape o navegar a otra pantalla.
 *
 * `esCajon` importa mas de lo que parece: el atributo `inert` solo debe
 * aplicarse a un cajon cerrado. Aplicarlo tambien en escritorio dejaba el menu
 * visible pero inerte, y ningun enlace respondia.
 */
@Injectable({ providedIn: 'root' })
export class MenuLateralService {

  private readonly _abierto = signal(false);
  private readonly _esCajon = signal(this.consultarAncho());

  readonly abierto = this._abierto.asReadonly();
  readonly esCajon = this._esCajon.asReadonly();

  /** Solo un cajon cerrado debe quedar fuera del alcance del teclado y el raton. */
  readonly inerte = computed(() => this._esCajon() && !this._abierto());

  constructor() {
    if (typeof window === 'undefined' || !window.matchMedia) return;

    const consulta = window.matchMedia(ANCHO_CAJON);

    const alCambiar = (e: MediaQueryListEvent) => {
      this._esCajon.set(e.matches);
      // Al ensanchar la ventana el menu vuelve a ser fijo: dejarlo "abierto"
      // haria que el velo siguiera tapando el contenido.
      if (!e.matches) this._abierto.set(false);
    };

    consulta.addEventListener('change', alCambiar);
    inject(DestroyRef).onDestroy(() => consulta.removeEventListener('change', alCambiar));
  }

  abrir(): void {
    this._abierto.set(true);
  }

  cerrar(): void {
    this._abierto.set(false);
  }

  alternar(): void {
    this._abierto.update(v => !v);
  }

  private consultarAncho(): boolean {
    return typeof window !== 'undefined' && !!window.matchMedia
      && window.matchMedia(ANCHO_CAJON).matches;
  }
}
