import { Component, OnInit, inject, ChangeDetectorRef, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';

import { NotificacionesService } from '../core/notificaciones.service';
import { environment } from '../../environments/environment';
import { SelectBuscadorComponent } from '../shared/select-buscador/select-buscador';
import * as L from 'leaflet';

@Component({
  selector: 'app-reginspeccion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SelectBuscadorComponent],
  templateUrl: './reginspeccion.html',
  styleUrls: ['./reginspeccion.css']
})
export class RegInspeccionComponent implements OnInit {

  /**
   * La aplicacion es zoneless: la deteccion de cambios se dispara con señales y
   * con eventos ligados en plantilla, no cuando llega una respuesta HTTP.
   * Asignar `this.adminZonales = data` dentro de un subscribe dejaba los datos
   * en memoria sin repintar nada: los desplegables se veian vacios aunque la
   * API respondiera. markForCheck() le dice a Angular que vuelva a mirar.
   */
  private readonly cd = inject(ChangeDetectorRef);

  private readonly ruta = inject(ActivatedRoute);

  @ViewChild('mapaDetalle') contenedorMapa?: ElementRef<HTMLDivElement>;

  private mapaDetalle?: L.Map;


  private readonly aviso = inject(NotificacionesService);

  // ── IDs globales ──
  idCodigoInternoGlobal: number = 0;
  idTecnicoGlobal: number = 0;

  // ── Maps: nombre → id para construir el body del POST ──
  mapArea           = new Map<string, number>();
  mapMotivo         = new Map<string, number>();
  mapTipoRodenticida= new Map<string, number>();
  mapNivelRoedores  = new Map<string, number>();
  mapNivelVoladores = new Map<string, number>();
  mapNivelCucarachas= new Map<string, number>();

  // ── Control de tabs por especie ──
  usaRoedores  : boolean = false;
  usaVoladores : boolean = false;
  usaCucarachas: boolean = false;

  // ── Tab activo ──
  tabActivo: 'roedores' | 'aves' | 'cucarachas' = 'roedores';

  // ── Listas para los selects ──
  areas           : Array<{ id: number; nombre: string }> = [];
  motivos         : Array<{ id: number; nombre: string }> = [];
  tiposRodenticida: Array<{ id: number; nombre: string }> = [];
  nivelesRoedores : Array<{ id: number; nombre: string }> = [];
  nivelesVoladores: Array<{ id: number; nombre: string }> = [];
  nivelesCucarachas: Array<{ id: number; nombre: string }> = [];

  // ── Imágenes ──
  /**
   * Imagenes nuevas: base64 mas el nombre y el peso del archivo, en un solo
   * objeto. Con dos listas paralelas, quitar una miniatura borraba la de al
   * lado en cuanto la lectura terminaba en otro orden.
   */
  rutasImagenesIns  : Array<{ url: string; nombre: string; peso: string }> = [];
  imagenesDenuncia  : string[] = [];   // URLs ya guardadas de la denuncia
  imagenesInspeccion: string[] = [];   // URLs ya guardadas de la inspección

  // ── Datos de denuncia (solo lectura) ──
  datosDenuncia: any = {};

  // ── Estado de carga ──
  cargando: boolean = false;

  // ── Formulario ──
  formulario!: FormGroup;

  // ── Vista y metadatos ──
  codigoDenuncia: string = '';
  tecnicoAsignado: string = '';
  fechaInspeccion: string = '';
  fechaAsignacion: string = '';

  /**
   * Adapta la respuesta de la API a lo que espera la plantilla.
   *
   * El backend devuelve snake_case (admin_zonal, tipo_denunciante) y la
   * plantilla lee camelCase. Coincidian por casualidad los campos de una sola
   * palabra —nombre, cedula, barrio— y quedaban vacios justo los compuestos.
   * Se normaliza aqui, que es el unico punto por el que pasa la ficha, en vez
   * de repartir el arreglo por toda la plantilla.
   */
  get denuncia(): any {

    const d = this.datosDenuncia;
    if (!d) return null;

    return {
      ...d,
      adminZonal: d.admin_zonal,
      tipoDenunciante: d.tipo_denunciante,
      // La ficha muestra una linea: las especies se unen por coma.
      especie: Array.isArray(d.especies) ? d.especies.join(', ') : d.especie,
      imagenes: this.imagenesDenuncia
    };
  }

  get previewsInspeccion(): Array<{ url: string; nombre: string; peso: string }> {
    return this.rutasImagenesIns;
  }

  /** Tope por inspeccion; el mismo numero se muestra en la zona de carga. */
  readonly maxImagenes = 5;

  /** 5 MB por imagen: por encima el envio se alarga sin ganar detalle. */
  private readonly maxBytesImagen = 5 * 1024 * 1024;

  /** Resalta la zona mientras se arrastra un archivo encima. */
  arrastrandoImagen = false;

  /** 0-100 mientras se leen las imagenes; null cuando no hay nada en curso. */
  progreso: number | null = null;
  mensajeProgreso = '';

  get nivelesInfestacion(): { id: number; nombre: string }[] {
    return this.tabActivo === 'roedores'
      ? this.nivelesRoedores
      : this.tabActivo === 'aves'
        ? this.nivelesVoladores
        : this.nivelesCucarachas;
  }

  get areasInspeccion(): { id: number; nombre: string }[] {
    return this.areas;
  }

  get motivosInspeccion(): { id: number; nombre: string }[] {
    return this.motivos;
  }

  get inspeccionForm(): FormGroup {
    return this.formulario;
  }

  // ── URL base del API ──
  private readonly API = `${environment.url_api}`;

  constructor(
    private fb    : FormBuilder,
    private http  : HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {

    // El trámite llega por la URL desde el listado ("Ver detalle" y
    // "Registrar inspección"). Sin esto la ficha se abria vacia: la pantalla
    // nunca sabia que tramite tenia que cargar.
    this.ruta.queryParamMap.subscribe(parametros => {

      const id = parametros.get('tramite');

      if (id) {
        this.cargarDatosDenuncia(id);
      }
    });
    this.inicializarFormulario();
    this.cargarCatalogos();
    this.escucharCambiosFormulario();

    this.fechaInspeccion = new Date().toLocaleDateString('es-EC', {
      day: '2-digit', month: '2-digit', year: 'numeric'
    });
    this.fechaAsignacion = this.fechaInspeccion;

    // Obtener el idTramite desde el estado de navegación o query params
    const nav = this.router.getCurrentNavigation();
    const idTramite = nav?.extras?.state?.['idTramite'];

    if (idTramite) {
      this.cargarDatosDenuncia(idTramite);
    }
  }

  // ════════════════════════════════
  // INICIALIZACIÓN DEL FORMULARIO
  // ════════════════════════════════

  inicializarFormulario(): void {
    this.formulario = this.fb.group({

      // Campos obligatorios
      areaInspeccion    : ['', Validators.required],
      motivoInspeccion  : ['', Validators.required],
      horaInspeccion    : ['', Validators.required],
      descripcion       : ['', [Validators.required, Validators.maxLength(500)]],
      conclusiones      : ['', Validators.required],
      recomendaciones   : ['', Validators.required],

      // Especificaciones de "Otros"
      especifqueRoedores   : [''],
      especifqueAves       : [''],
      especifqueCucarachas : [''],
      otrosR  : [false],
      otrosV  : [false],
      otrosC  : [false],

      // Tipo y cantidad de rodenticida
      tipoRodenticida    : [''],
      cantidadRodenticida: [''],

      // Niveles de infestación
      nivelRoedores  : [''],
      nivelAves      : [''],
      nivelCucarachas: [''],

      // Evidencias roedores
      madriguera: [false],
      heces     : [false],
      senderos  : [false],
      roeduras  : [false],

      // Evidencias aves
      nidos   : [false],
      hecesV  : [false],
      plumas  : [false],
      picoteos: [false],

      // Evidencias cucarachas
      ootecas   : [false],
      cucarachas: [false]
    });
  }

  // ════════════════════════════════
  // VALIDACIÓN REACTIVA
  // ════════════════════════════════

  private escucharCambiosFormulario(): void {
    this.formulario.valueChanges.subscribe(() => {
      this.actualizarValidadoresCondicionales();
    });
  }

  private actualizarValidadoresCondicionales(): void {

    const tipoCtrl     = this.formulario.get('tipoRodenticida')!;
    const cantidadCtrl = this.formulario.get('cantidadRodenticida')!;
    const espRCtrl     = this.formulario.get('especifqueRoedores')!;
    const espVCtrl     = this.formulario.get('especifqueAves')!;
    const espCCtrl     = this.formulario.get('especifqueCucarachas')!;

    // Rodenticida obligatorio si la especie roedores está activa
    if (this.usaRoedores) {
      tipoCtrl.setValidators(Validators.required);
      cantidadCtrl.setValidators(Validators.required);
    } else {
      tipoCtrl.clearValidators();
      cantidadCtrl.clearValidators();
    }

    // "Especifique" obligatorio si el checkbox "Otros" está marcado
    espRCtrl.setValidators(
      this.formulario.get('otrosR')?.value ? Validators.required : null
    );
    espVCtrl.setValidators(
      this.formulario.get('otrosV')?.value ? Validators.required : null
    );
    espCCtrl.setValidators(
      this.formulario.get('otrosC')?.value ? Validators.required : null
    );

    // Actualizar sin emitir evento para evitar bucle infinito
    [tipoCtrl, cantidadCtrl, espRCtrl, espVCtrl, espCCtrl].forEach(c => {
      c.updateValueAndValidity({ emitEvent: false });
    });
  }

  get formularioValido(): boolean {
    return this.formulario.valid;
  }

  // ════════════════════════════════
  // CARGA DE CATÁLOGOS
  // ════════════════════════════════

  private cargarCatalogos(): void {
    this.cargarAreaIns();
    this.cargarMotivoIns();
    this.cargarTipoRodenticida();
    this.cargarNivel(1);
    this.cargarNivel(2);
    this.cargarNivel(3);
  }

  cargarAreaIns(): void {
    this.http.get<any[]>(`${this.API}/area-inspeccion`).subscribe({
      next: (data) => {
        // El catalogo responde {id, nombre}; se leia item.id_area y todas las opciones valian undefined.
        this.areas = data.map(item => ({ id: item.id, nombre: item.nombre }));

        this.cd.markForCheck();
      },
      error: (err) => console.error('Error cargando áreas:', err)
    });
  }

  cargarMotivoIns(): void {
    this.http.get<any[]>(`${this.API}/motivo-inspeccion`).subscribe({
      next: (data) => {
        this.motivos = data.map(item => ({ id: item.id, nombre: item.nombre }));

        this.cd.markForCheck();
      },
      error: (err) => console.error('Error cargando motivos:', err)
    });
  }

  cargarTipoRodenticida(): void {
    this.http.get<any[]>(`${this.API}/tipo-rodenticida`).subscribe({
      next: (data) => {
        this.tiposRodenticida = data.map(item => ({ id: item.id, nombre: item.nombre }));

        this.cd.markForCheck();
      },
      error: (err) => console.error('Error cargando rodenticidas:', err)
    });
  }

  cargarNivel(idEspecie: number): void {
    this.http.get<any[]>(`${this.API}/nivel-infestacion/${idEspecie}`).subscribe({
      next: (data) => {
        const items = data.map(item => ({
          nombre: item.descripcion ? `${item.nombre} - ${item.descripcion}` : item.nombre,
          id   : item.id
        }));

        if (idEspecie === 1) {
          this.nivelesRoedores = items;
        }

        if (idEspecie === 2) {
          this.nivelesVoladores = items;
        }

        if (idEspecie === 3) {
          this.nivelesCucarachas = items;
        }

        this.cd.markForCheck();
      },
      error: (err) => console.error(`Error cargando niveles especie ${idEspecie}:`, err)
    });
  }

  // ════════════════════════════════
  // CARGA DE DATOS DE DENUNCIA
  // ════════════════════════════════

  cargarDatosDenuncia(idTramite: string): void {
    this.http.get<any>(`${this.API}/tramite/${idTramite}`).subscribe({
      next: (data) => {
        this.datosDenuncia          = data;
        this.idCodigoInternoGlobal  = data.id_codigo_interno;
        this.idTecnicoGlobal        = data.id_tecnico ?? 0;

        this.mostrarTabsPorEspecies(this.idCodigoInternoGlobal);
        this.cargarImagenDenuncia(this.idCodigoInternoGlobal);
        this.cargarImagenInspeccion(this.idCodigoInternoGlobal);

        this.cd.markForCheck();

        // El contenedor ya existe en el DOM; el mapa se pinta cuando llegan
        // las coordenadas, no antes.
        setTimeout(() => this.pintarMapa(data.latitud, data.longitud));
      },
      error: (err) => console.error('Error cargando datos del trámite:', err)
    });
  }

  mostrarTabsPorEspecies(idCodigoInterno: number): void {
    this.http.get<any[]>(`${this.API}/tramite-especie/${idCodigoInterno}`).subscribe({
      next: (data) => {
        this.usaRoedores   = false;
        this.usaVoladores  = false;
        this.usaCucarachas = false;

        data.forEach(especie => {
          switch (especie.id_especie) {
            case 1: this.usaRoedores   = true; break;
            case 2: this.usaVoladores  = true; break;
            case 3: this.usaCucarachas = true; break;
          }
        });

        // Establecer el primer tab activo disponible
        if      (this.usaRoedores)   this.tabActivo = 'roedores';
        else if (this.usaVoladores)  this.tabActivo = 'aves';
        else if (this.usaCucarachas) this.tabActivo = 'cucarachas';

        // Recalcular validadores tras conocer las especies
        this.actualizarValidadoresCondicionales();
      },
      error: (err) => console.error('Error cargando especies:', err)
    });
  }

  // ════════════════════════════════
  // GESTIÓN DE IMÁGENES
  // ════════════════════════════════

  cargarImagenDenuncia(idCodigoInterno: number): void {
    this.http.get<any[]>(`${this.API}/imagen-denuncia/${idCodigoInterno}`).subscribe({
      next: (data) => {
        this.imagenesDenuncia = data.map(img => img.ruta_imagen);

        this.cd.markForCheck();
      },
      error: (err) => console.error('Error cargando imágenes de denuncia:', err)
    });
  }

  cargarImagenInspeccion(idCodigoInterno: number): void {
    this.http.get<any[]>(`${this.API}/imagen-inspeccion/${idCodigoInterno}`).subscribe({
      next: (data) => {
        this.imagenesInspeccion = data.map(img => img.ruta_imagen);

        this.cd.markForCheck();
      },
      error: (err) => console.error('Error cargando imágenes de inspección:', err)
    });
  }

  seleccionarImagen(event: Event): void {

    const input = event.target as HTMLInputElement;

    this.agregarImagenes(input.files);

    // Sin esto no se puede volver a elegir el mismo archivo tras quitarlo.
    input.value = '';
  }

  seleccionarImagenInspeccion(event: Event): void {
    this.seleccionarImagen(event);
  }

  alArrastrar(event: DragEvent): void {
    event.preventDefault();
    this.arrastrandoImagen = true;
  }

  alSalirArrastre(event: DragEvent): void {
    event.preventDefault();
    this.arrastrandoImagen = false;
  }

  alSoltar(event: DragEvent): void {
    event.preventDefault();
    this.arrastrandoImagen = false;
    this.agregarImagenes(event.dataTransfer?.files ?? null);
  }

  private agregarImagenes(archivos: FileList | null): void {

    if (!archivos || archivos.length === 0) return;

    const aceptados: File[] = [];

    for (const archivo of Array.from(archivos)) {

      if (this.rutasImagenesIns.length + aceptados.length >= this.maxImagenes) {
        this.aviso.aviso(
          `Solo se permiten ${this.maxImagenes} imágenes por inspección.`);
        break;
      }

      if (!archivo.type.startsWith('image/')) {
        this.aviso.aviso(`"${archivo.name}" no es una imagen.`);
        continue;
      }

      if (archivo.size > this.maxBytesImagen) {
        this.aviso.aviso(
          `"${archivo.name}" pesa ${this.pesoLegible(archivo.size)}: el máximo es 5 MB.`);
        continue;
      }

      aceptados.push(archivo);
    }

    if (aceptados.length === 0) {
      this.cd.markForCheck();
      return;
    }

    this.progreso = 0;
    this.mensajeProgreso = aceptados.length === 1
        ? 'Preparando la imagen...'
        : `Preparando ${aceptados.length} imágenes...`;

    const inicio = Date.now();
    let leidas = 0;

    aceptados.forEach(archivo => {

      // La entrada se reserva ya: la lectura es asincrona y el orden de
      // llegada no tiene por que ser el de seleccion.
      const entrada = {
        url: '',
        nombre: archivo.name,
        peso: this.pesoLegible(archivo.size)
      };

      this.rutasImagenesIns.push(entrada);

      const lector = new FileReader();

      const terminar = () => {
        leidas++;
        this.progreso = Math.round((leidas / aceptados.length) * 100);
        if (leidas === aceptados.length) {
          this.cerrarProgreso(inicio);
        }
        // Zoneless: sin esto la miniatura nunca aparecia en pantalla.
        this.cd.markForCheck();
      };

      lector.onload = () => {
        entrada.url = lector.result as string;
        terminar();
      };

      lector.onerror = () => {
        this.aviso.aviso(`No se pudo leer "${archivo.name}".`);
        this.eliminarImagenInspeccion(this.rutasImagenesIns.indexOf(entrada));
        terminar();
      };

      lector.readAsDataURL(archivo);
    });

    this.cd.markForCheck();
  }


  /**
   * Oculta la barra dejandola visible un instante.
   *
   * Leer un par de fotos tarda menos que un repintado, asi que la barra pasaba
   * de aparecer a desaparecer dentro del mismo ciclo y no llegaba a verse
   * nunca. El porcentaje es real; lo unico que se sostiene es el ultimo tramo.
   */
  private cerrarProgreso(desde: number): void {

    const MINIMO_VISIBLE = 500;
    const restante = Math.max(0, MINIMO_VISIBLE - (Date.now() - desde));

    setTimeout(() => {
      this.progreso = null;
      this.mensajeProgreso = '';
      this.cd.markForCheck();
    }, restante);
  }

  /** Tamaño en la unidad que se entiende de un vistazo. */
  pesoLegible(bytes: number): string {

    if (bytes < 1024) return `${bytes} B`;

    if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;

    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  /** Ayuda de la pantalla, para el tecnico que la usa en campo. */
  mostrarAyuda(): void {

    this.aviso.ayuda('Cómo registrar la inspección', `
      <ol class="lista-ayuda">
        <li><b>Arriba, la denuncia.</b> Es solo lectura: es lo que reportó el
            ciudadano, con sus fotos y su ubicación.</li>
        <li><b>Fecha, hora y motivo.</b> Registra cuándo hiciste la visita, no
            cuándo llenas el formulario.</li>
        <li><b>Evidencia por especie.</b> Cada pestaña —roedores, aves,
            cucarachas— tiene sus propios indicios y su nivel de infestación.
            Marca solo lo que constataste.</li>
        <li><b>Fotos.</b> Arrástralas o pulsa la zona de carga. Se ven completas,
            sin recorte, para que se aprecie el foco.</li>
        <li><b>Guardar.</b> El trámite pasa a inspeccionado y el ciudadano lo ve
            reflejado en el seguimiento de su código.</li>
      </ol>
      <p class="nota-ayuda">Los campos con <b>*</b> son obligatorios.</p>
    `);
  }

  toggleEvidencia(event: Event, especie: 'roedores' | 'aves' | 'cucarachas', evidencia: string): void {
    const input = event.target as HTMLInputElement;
    const checked = input.checked;

    const mapping: Record<string, string> = {
      madriguera: 'madriguera',
      heces: 'heces',
      senderos: 'senderos',
      roeduras: 'roeduras',
      otros: 'otrosR',
      nidos: 'nidos',
      plumas: 'plumas',
      picoteos: 'picoteos',
      ootecas: 'ootecas',
      cucarachas: 'cucarachas'
    };

    const controlName = mapping[evidencia];
    if (controlName && this.formulario.contains(controlName)) {
      this.formulario.get(controlName)?.setValue(checked);
    }
  }

  guardarInspeccion(): void {
    this.guardar();
  }

  eliminarImagenInspeccion(index: number): void {
    if (index < 0) return;
    this.rutasImagenesIns.splice(index, 1);
    this.cd.markForCheck();
  }

  // ════════════════════════════════
  // TABS
  // ════════════════════════════════

  cambiarTab(tab: 'roedores' | 'aves' | 'cucarachas'): void {
    this.tabActivo = tab;
  }

  // ════════════════════════════════
  // GUARDAR
  // ════════════════════════════════

  guardar(): void {

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.aviso.aviso('Completa los campos obligatorios antes de guardar.');
      return;
    }

    this.cargando = true;

    const v = this.formulario.value;

    const body = {

      id_codigo_interno: this.idCodigoInternoGlobal,
      id_tecnico        : this.idTecnicoGlobal,

      fecha_inspeccion: new Date().toISOString(),
      hora_inspeccion : v.hora,

      id_area  : v.areaInspeccion,
      id_motivo: v.motivoInspeccion,

      descripcion     : v.descripcion,
      conclusiones    : v.conclusiones,
      recomendaciones : v.recomendaciones,

      tipo_rodenticida  : v.tipoRodenticida || null,
      cantidad_rodenticida: v.cantidadRodenticida || null,

      evidencias: {
        // Roedores
        madriguera: v.madriguera,
        heces     : v.heces,
        senderos  : v.senderos,
        roeduras  : v.roeduras,
        otrosR    : v.otrosR,
        espRoedores: v.otrosR ? v.especifqueRoedores : null,
        // Aves
        nidos    : v.nidos,
        hecesV   : v.hecesV,
        plumas   : v.plumas,
        picoteos : v.picoteos,
        otrosV   : v.otrosV,
        espVoladores: v.otrosV ? v.especifqueAves : null,
        // Cucarachas
        ootecas    : v.ootecas,
        cucarachas : v.cucarachas,
        otrosC     : v.otrosC,
        espCucarachas: v.otrosC ? v.especifqueCucarachas : null
      },

      niveles: {
        roedores  : v.nivelRoedores || null,
        voladores : v.nivelAves || null,
        cucarachas: v.nivelCucarachas || null
      },

      imagenes: this.rutasImagenesIns.map(i => i.url)
    };

    this.http.post(`${this.API}/guardar-inspeccion`, body).subscribe({
      next: () => {
        this.cargando = false;
        this.aviso.exito('Inspección guardada correctamente.');
        this.volver();

        this.cd.markForCheck();
      },
      error: (err) => {
        this.cargando = false;
        console.error('Error al guardar:', err);
        this.aviso.error('No se pudo guardar la inspección. Intenta de nuevo.');
      }
    });
  }

  // ════════════════════════════════
  // NAVEGACIÓN
  // ════════════════════════════════

  volver(): void {
    window.history.back();
  }

  /**
   * Mapa de solo lectura con la ubicacion de la denuncia.
   *
   * Se invalida el tamaño despues de crearlo porque el contenedor puede haber
   * cambiado de alto al llegar los datos, y Leaflet dibujaria las teselas con
   * las medidas viejas dejando el mapa en gris.
   */
  private pintarMapa(latitud: unknown, longitud: unknown): void {

    const lat = Number(latitud);
    const lon = Number(longitud);

    if (!this.contenedorMapa || !isFinite(lat) || !isFinite(lon) || (lat === 0 && lon === 0)) {
      return;
    }

    this.mapaDetalle?.remove();

    this.mapaDetalle = L.map(this.contenedorMapa.nativeElement, {
      center: [lat, lon],
      zoom: 17,
      // Es una ficha de consulta: no se navega ni se edita.
      dragging: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      zoomControl: false,
      attributionControl: false
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19
    }).addTo(this.mapaDetalle);

    L.marker([lat, lon]).addTo(this.mapaDetalle);

    setTimeout(() => this.mapaDetalle?.invalidateSize(), 200);
  }
}
