import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { CatalogosApi, DefinicionCatalogo, ItemCatalogo } from '../core/catalogos.api';
import { NotificacionesService } from '../core/notificaciones.service';
import { CampoComponent } from '../shared/campo/campo';
import { PermisosService } from '../core/permisos.service';
import { SelectBuscadorComponent } from '../shared/select-buscador/select-buscador';

type Modo = 'crear' | 'editar';

/** Filas por pagina de la tabla. */
const POR_PAGINA = 15;

/** Minusculas y sin tildes, para que "iñaquito" encuentre "IÑAQUITO" y "inaquito". */
const normalizar = (t: unknown) =>
  String(t ?? '').normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase();

/**
 * Administracion de los catalogos que alimentan los formularios.
 *
 * Es una sola pantalla para los 13 porque comparten forma: cambia la etiqueta,
 * si tienen padre y si tienen descripcion. El backend publica esa descripcion,
 * asi que anadir un catalogo alli no obliga a tocar nada aqui.
 */
@Component({
  selector: 'app-catalogos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CampoComponent, SelectBuscadorComponent],
  templateUrl: './catalogos.html'
})
export class CatalogosComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(CatalogosApi);
  private readonly aviso = inject(NotificacionesService);
  private readonly permisos = inject(PermisosService);

  readonly puedeCrear = computed(() => this.permisos.puede('catalogos', 'CREAR'));
  readonly puedeEditar = computed(() => this.permisos.puede('catalogos', 'EDITAR'));
  readonly puedeEliminar = computed(() => this.permisos.puede('catalogos', 'ELIMINAR'));

  readonly definiciones = signal<DefinicionCatalogo[]>([]);
  readonly seleccionado = signal<DefinicionCatalogo | null>(null);

  readonly items = signal<ItemCatalogo[]>([]);
  /** Filas del catalogo padre, para el desplegable. */
  readonly padres = signal<ItemCatalogo[]>([]);

  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly filtro = signal('');
  readonly filtroPadre = signal<number | null>(null);

  readonly panelAbierto = signal(false);
  readonly modo = signal<Modo>('crear');
  readonly editando = signal<ItemCatalogo | null>(null);

  private readonly revision = signal(0);

  formulario!: FormGroup;

  readonly porPagina = POR_PAGINA;

  /**
   * Pagina pedida. La tabla se pagina en el navegador: el catalogo llega
   * entero (los barrios son ~2.150 filas) y pintarlo de golpe dejaba la
   * pantalla lenta y obligaba a desplazarse sin fin.
   */
  private readonly paginaPedida = signal(1);

  /** Todo lo que coincide con la busqueda, sin paginar. */
  readonly visibles = computed<ItemCatalogo[]>(() => {

    const texto = normalizar(this.filtro().trim());
    if (!texto) return this.items();

    return this.items().filter(i =>
      [i.nombre, i.nombrePadre, i.descripcion]
        .some(c => normalizar(c).includes(texto)));
  });

  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.visibles().length / POR_PAGINA)));

  /** Acotada: al borrar la ultima fila de la ultima pagina no se queda en una pagina vacia. */
  readonly pagina = computed(() => Math.min(this.paginaPedida(), this.totalPaginas()));

  readonly filasPagina = computed(() => {
    const desde = (this.pagina() - 1) * POR_PAGINA;
    return this.visibles().slice(desde, desde + POR_PAGINA);
  });

  readonly desde = computed(() => this.visibles().length ? (this.pagina() - 1) * POR_PAGINA + 1 : 0);
  readonly hasta = computed(() => Math.min(this.pagina() * POR_PAGINA, this.visibles().length));

  /** Numeros a mostrar: primera, ultima y dos a cada lado de la actual; null es un hueco (…). */
  readonly numerosPagina = computed<(number | null)[]>(() => {
    const total = this.totalPaginas();
    const actual = this.pagina();
    const numeros: (number | null)[] = [];

    for (let p = 1; p <= total; p++) {
      if (p === 1 || p === total || Math.abs(p - actual) <= 2) {
        numeros.push(p);
      } else if (numeros[numeros.length - 1] !== null) {
        numeros.push(null);
      }
    }
    return numeros;
  });

  irAPagina(p: number): void {
    this.paginaPedida.set(Math.min(Math.max(1, p), this.totalPaginas()));
  }

  buscar(texto: string): void {
    this.filtro.set(texto);
    this.paginaPedida.set(1);
  }

  ngOnInit(): void {

    this.formulario = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(150)]],
      idPadre: [null as number | null],
      descripcion: ['']
    });

    this.formulario.events.subscribe(() => this.revision.update(v => v + 1));

    this.api.definiciones().subscribe({
      next: d => {
        this.definiciones.set(d ?? []);
        if (d?.length) this.elegir(d[0]);
      },
      error: e => this.aviso.errorHttp(e, 'No se pudieron cargar los catálogos.')
    });
  }

  elegir(d: DefinicionCatalogo): void {
    this.seleccionado.set(d);
    this.filtro.set('');
    this.paginaPedida.set(1);
    this.filtroPadre.set(null);
    this.cargar();
    this.cargarPadres();
  }

  cargar(): void {

    const d = this.seleccionado();
    if (!d) return;

    this.cargando.set(true);

    this.api.listar(d.clave, this.filtroPadre()).subscribe({
      next: l => { this.items.set(l ?? []); this.cargando.set(false); },
      error: e => {
        this.cargando.set(false);
        this.aviso.errorHttp(e, `No se pudo cargar ${d.etiqueta.toLowerCase()}.`);
      }
    });
  }

  private cargarPadres(): void {

    const d = this.seleccionado();

    if (!d?.clavePadre) {
      this.padres.set([]);
      return;
    }

    this.api.listar(d.clavePadre).subscribe({
      next: l => this.padres.set(l ?? []),
      error: () => this.padres.set([])
    });
  }

  filtrarPorPadre(valor: string | number | null): void {
    this.filtroPadre.set(valor ? Number(valor) : null);
    this.paginaPedida.set(1);
    this.cargar();
  }

  // ---------------------------------------------------------------- panel

  campo(nombre: string) {
    return this.formulario?.get(nombre) ?? null;
  }

  abrirCrear(): void {
    this.modo.set('crear');
    this.editando.set(null);
    this.formulario.reset({ nombre: '', idPadre: this.filtroPadre(), descripcion: '' });
    this.panelAbierto.set(true);
  }

  abrirEditar(item: ItemCatalogo): void {
    this.modo.set('editar');
    this.editando.set(item);
    this.formulario.patchValue({
      nombre: item.nombre,
      idPadre: item.idPadre ?? null,
      descripcion: item.descripcion ?? ''
    });
    this.panelAbierto.set(true);
  }

  guardar(): void {

    const d = this.seleccionado();
    if (!d) return;

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const v = this.formulario.value;

    const datos: ItemCatalogo = {
      nombre: (v.nombre ?? '').trim(),
      idPadre: d.clavePadre ? (v.idPadre != null ? Number(v.idPadre) : null) : null,
      descripcion: d.tieneDescripcion ? (v.descripcion ?? '').trim() : undefined
    };

    this.guardando.set(true);

    const peticion = this.modo() === 'crear'
      ? this.api.crear(d.clave, datos)
      : this.api.actualizar(d.clave, this.editando()!.id!, datos);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.panelAbierto.set(false);
        this.aviso.exito(this.modo() === 'crear' ? 'Registro creado.' : 'Registro actualizado.');
        this.cargar();
      },
      error: e => {
        this.guardando.set(false);
        this.aviso.errorHttp(e, 'No se pudo guardar el registro.');
      }
    });
  }

  async borrar(item: ItemCatalogo): Promise<void> {

    const d = this.seleccionado();
    if (!d) return;

    const ok = await this.aviso.confirmarPeligro(
      'Eliminar del catálogo',
      `Se eliminará «${item.nombre}». Si algún registro lo está usando, `
      + 'la operación se rechazará y no pasará nada.',
      'Eliminar');

    if (!ok) return;

    this.api.borrar(d.clave, item.id!).subscribe({
      next: () => {
        this.aviso.exito(`«${item.nombre}» se eliminó.`);
        this.cargar();
      },
      // El backend explica por qué no se puede: se muestra tal cual.
      error: e => this.aviso.errorHttp(e, 'No se pudo eliminar.')
    });
  }

  invalido(campo: string): boolean {
    this.revision();
    const c = this.formulario?.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  iconoCatalogo(clave: string): string {
    const iconos: Record<string, string> = {
      'admin-zonal': 'fa-city',
      'parroquia': 'fa-map-location-dot',
      'barrio': 'fa-location-dot',
      'especie': 'fa-bug',
      'motivo': 'fa-clipboard-question',
      'area': 'fa-vector-square',
      'predio': 'fa-building',
      'tipo-denunciante': 'fa-user-tag',
      'tipo-parroquia': 'fa-map',
      'tipo-rodenticida': 'fa-flask',
      'dependencia': 'fa-sitemap',
      'evidencia': 'fa-camera',
      'nivel-infestacion': 'fa-gauge-high'
    };
    return iconos[clave] ?? 'fa-list';
  }
}
