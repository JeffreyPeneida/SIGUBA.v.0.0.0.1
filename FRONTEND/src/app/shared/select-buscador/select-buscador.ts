import {
  Component, ElementRef, computed, effect, forwardRef, inject, input, output, signal, viewChild
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/** Cuantas opciones se pintan a la vez: con miles de barrios el panel se atascaba. */
const MAX_VISIBLES = 200;

/** Minusculas y sin tildes: "Iñaquito" se encuentra escribiendo "inaquito". */
function normalizar(texto: unknown): string {
  return String(texto ?? '').normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase();
}

/**
 * Desplegable con buscador.
 *
 * Sustituye a <select> en los catalogos: con 66 parroquias o cientos de
 * barrios, bajar la lista a ojo era lento y facil de fallar. Se usa igual que
 * un select con formControlName; fuera de un formulario, con [seleccion] y
 * (cambio).
 *
 * Devuelve el valor tal cual viene en la opcion (normalmente el id numerico),
 * no convertido a texto como hacia el <select> nativo. Las comparaciones se
 * hacen como texto, asi que un valor '3' guardado antes sigue encontrando su
 * opcion 3.
 */
@Component({
  selector: 'app-select-buscador',
  standalone: true,
  templateUrl: './select-buscador.html',
  styleUrl: './select-buscador.css',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SelectBuscadorComponent),
    multi: true
  }],
  host: {
    '(document:mousedown)': 'clicFuera($event)',
    '(window:resize)': 'cerrar()',
    '[class.abierto]': 'abierto()'
  }
})
export class SelectBuscadorComponent implements ControlValueAccessor {

  readonly opciones = input<any[] | null | undefined>([]);

  /** Propiedad de la opcion que se guarda en el control. */
  readonly campoValor = input('id');

  /** Propiedad de la opcion que se muestra. */
  readonly campoTexto = input('nombre');

  readonly placeholder = input('Seleccione');

  /** Texto de la opcion "ninguna"; null para no ofrecerla. */
  readonly opcionVacia = input<string | null>('Seleccione');

  /** Valor que representa "ninguna": '' en la mayoria de formularios, null en otros. */
  readonly valorVacio = input<unknown>('');

  /** Clases del boton. Sin ellas toma el estilo propio (el de los formularios de denuncia). */
  readonly clase = input('');

  /** Uso fuera de un formulario: valor inicial y aviso de cambio. */
  readonly seleccion = input<unknown>(undefined);
  readonly cambio = output<any>();

  readonly abierto = signal(false);
  readonly deshabilitado = signal(false);
  readonly busqueda = signal('');
  readonly resaltado = signal(0);

  /**
   * Posicion del panel. Va con position: fixed porque los formularios tienen
   * contenedores con overflow: hidden (las pestanas de la inspeccion) que lo
   * recortaban.
   */
  readonly posicion = signal<Record<string, string>>({});

  private readonly valor = signal<unknown>('');

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly buscador = viewChild<ElementRef<HTMLInputElement>>('buscador');
  private readonly lista = viewChild<ElementRef<HTMLElement>>('lista');

  private alCambiar: (v: unknown) => void = () => {};
  private alTocar: () => void = () => {};

  readonly seleccionada = computed(() => {
    const v = this.valor();
    if (v === null || v === undefined || v === '') return null;
    return (this.opciones() ?? []).find(o => String(o?.[this.campoValor()]) === String(v)) ?? null;
  });

  readonly textoSeleccionado = computed(() => {
    const o = this.seleccionada();
    return o ? String(o[this.campoTexto()] ?? '') : '';
  });

  readonly filtradas = computed(() => {
    const q = normalizar(this.busqueda().trim());
    const todas = this.opciones() ?? [];
    return q ? todas.filter(o => normalizar(o?.[this.campoTexto()]).includes(q)) : todas;
  });

  readonly visibles = computed(() => this.filtradas().slice(0, MAX_VISIBLES));

  /** Filas navegables: la opcion vacia (si hay y no se esta buscando) y las visibles. */
  readonly conVacia = computed(() => this.opcionVacia() !== null && !this.busqueda().trim());

  constructor() {
    effect(() => {
      const s = this.seleccion();
      if (s !== undefined) this.valor.set(s);
    });
  }

  // ---------------------------------------------------- ControlValueAccessor

  writeValue(v: unknown): void {
    this.valor.set(v ?? this.valorVacio());
  }

  registerOnChange(fn: (v: unknown) => void): void {
    this.alCambiar = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.alTocar = fn;
  }

  setDisabledState(deshabilitado: boolean): void {
    this.deshabilitado.set(deshabilitado);
    if (deshabilitado) this.abierto.set(false);
  }

  // ------------------------------------------------------------------ panel

  alternar(): void {
    this.abierto() ? this.cerrar() : this.abrir();
  }

  abrir(textoInicial = ''): void {
    if (this.deshabilitado()) return;

    this.busqueda.set(textoInicial);
    this.calcularPosicion();
    this.abierto.set(true);
    document.addEventListener('scroll', this.alDesplazar, true);

    // Arranca sobre lo ya elegido, para no perder el sitio en listas largas.
    const i = this.visibles().indexOf(this.seleccionada());
    this.resaltado.set(i >= 0 ? i + (this.conVacia() ? 1 : 0) : 0);

    setTimeout(() => {
      this.buscador()?.nativeElement.focus();
      this.desplazarAResaltado();
    });
  }

  cerrar(devolverFoco = false): void {
    if (!this.abierto()) return;
    this.abierto.set(false);
    document.removeEventListener('scroll', this.alDesplazar, true);
    this.alTocar();
    if (devolverFoco) {
      this.host.nativeElement.querySelector<HTMLButtonElement>('.sb-trigger')?.focus();
    }
  }

  elegir(opcion: any | null): void {
    const v = opcion === null ? this.valorVacio() : opcion[this.campoValor()];
    this.valor.set(v);
    this.alCambiar(v);
    this.cambio.emit(v);
    this.cerrar(true);
  }

  buscar(texto: string): void {
    this.busqueda.set(texto);
    this.resaltado.set(0);
  }

  clicFuera(evento: MouseEvent): void {
    if (this.abierto() && !this.host.nativeElement.contains(evento.target as Node)) {
      this.cerrar();
    }
  }

  // ---------------------------------------------------------------- teclado

  teclaEnBoton(evento: KeyboardEvent): void {
    if (['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(evento.key)) {
      evento.preventDefault();
      this.abrir();
    } else if (evento.key.length === 1 && !evento.ctrlKey && !evento.metaKey && !evento.altKey) {
      // Escribir sobre el boton cerrado empieza a buscar directamente.
      evento.preventDefault();
      this.abrir(evento.key);
    }
  }

  teclaEnBuscador(evento: KeyboardEvent): void {
    const total = this.visibles().length + (this.conVacia() ? 1 : 0);

    switch (evento.key) {
      case 'ArrowDown':
        evento.preventDefault();
        this.resaltado.set(Math.min(this.resaltado() + 1, total - 1));
        this.desplazarAResaltado();
        break;
      case 'ArrowUp':
        evento.preventDefault();
        this.resaltado.set(Math.max(this.resaltado() - 1, 0));
        this.desplazarAResaltado();
        break;
      case 'Enter': {
        evento.preventDefault();
        if (total === 0) return;
        const i = this.resaltado();
        if (this.conVacia()) {
          this.elegir(i === 0 ? null : this.visibles()[i - 1]);
        } else {
          this.elegir(this.visibles()[i]);
        }
        break;
      }
      case 'Escape':
        evento.preventDefault();
        this.cerrar(true);
        break;
      case 'Tab':
        this.cerrar();
        break;
    }
  }

  esElegida(opcion: any): boolean {
    return opcion === this.seleccionada();
  }

  /** Al desplazar la pagina el panel sigue al boton; el scroll de la propia lista no cuenta. */
  private readonly alDesplazar = (evento: Event) => {
    if (!this.host.nativeElement.contains(evento.target as Node)) this.calcularPosicion();
  };

  private calcularPosicion(): void {
    const boton = this.host.nativeElement.querySelector('.sb-trigger') as HTMLElement | null;
    if (!boton) return;

    const r = boton.getBoundingClientRect();
    const altoPanel = 340;
    const abajo = window.innerHeight - r.bottom;
    const haciaArriba = abajo < altoPanel && r.top > abajo;

    this.posicion.set({
      left: `${r.left}px`,
      width: `${r.width}px`,
      ...(haciaArriba
        ? { bottom: `${window.innerHeight - r.top + 6}px`, top: 'auto' }
        : { top: `${r.bottom + 6}px`, bottom: 'auto' })
    });
  }

  private desplazarAResaltado(): void {
    setTimeout(() => {
      this.lista()?.nativeElement
        .querySelector<HTMLElement>(`[data-indice="${this.resaltado()}"]`)
        ?.scrollIntoView({ block: 'nearest' });
    });
  }
}
