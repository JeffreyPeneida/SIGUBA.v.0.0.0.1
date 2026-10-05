import { AfterViewInit, Component, ElementRef, NgZone, OnDestroy, OnInit, ViewChild, inject, signal, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { NotificacionesService } from '../core/notificaciones.service';
import * as L from 'leaflet';
import { environment } from '../../environments/environment';
import { SelectBuscadorComponent } from '../shared/select-buscador/select-buscador';

/**
 * Leaflet resuelve las rutas de sus marcadores contra la raiz del sitio, y
 * pedia /marker-icon.png y /marker-shadow.png, que devolvian 404: el marcador
 * salia como un icono roto. Las imagenes se copiaron a public/ y aqui se le
 * dice explicitamente donde estan.
 */
/** Centro aproximado del Distrito Metropolitano, para abrir el mapa ahi. */
const CENTRO_QUITO = { lat: -0.2202, lon: -78.5123 };

const ICONO_MARCADOR = L.icon({
  iconUrl: 'marker-icon.png',
  iconRetinaUrl: 'marker-icon-2x.png',
  shadowUrl: 'marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

L.Marker.prototype.options.icon = ICONO_MARCADOR;

@Component({
  selector: 'app-registrar-denuncia',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule, SelectBuscadorComponent],
  templateUrl: './regis_denuncia.html',
  styleUrls: ['./regis_denuncia.css']
})


export class RegistrarDenunciaComponent implements OnInit, AfterViewInit, OnDestroy {

  /**
   * La aplicacion es zoneless: la deteccion de cambios se dispara con señales y
   * con eventos ligados en plantilla, no cuando llega una respuesta HTTP.
   * Asignar `this.adminZonales = data` dentro de un subscribe dejaba los datos
   * en memoria sin repintar nada: los desplegables se veian vacios aunque la
   * API respondiera. markForCheck() le dice a Angular que vuelva a mirar.
   */
  private readonly cd = inject(ChangeDetectorRef);


  private readonly aviso = inject(NotificacionesService);

  /**
   * Avanza con cada cambio del formulario. La aplicacion es zoneless y los
   * formularios reactivos avisan por RxJS: sin esto los mensajes de error
   * calculados en la plantilla no se repintarian nunca.
   */
  readonly revision = signal(0);

  /** Tras el primer intento de envio los errores se muestran aunque no se haya tocado el campo. */
  readonly intentoEnvio = signal(false);

  /** Etiqueta legible de cada campo, para el resumen de lo que falta. */
  private readonly etiquetas: Record<string, string> = {
    nombre: 'Nombre',
    apellido: 'Apellido',
    cedula: 'Cédula',
    direccion: 'Dirección',
    referencia: 'Referencia',
    narracion: 'Narración de los hechos',
    adminZonal: 'Administración zonal',
    parroquia: 'Parroquia',
    barrio: 'Barrio',
    tipoDenunciante: 'Tipo de denunciante',
    dependencia: 'Dependencia',
    predio: 'Tipo de predio',
    especiesSeleccionadas: 'Especie afectada'
  };

  /** True si el campo esta mal y ya procede avisar. */
  invalido(campo: string): boolean {
    this.revision();
    const c = this.denunciaForm?.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty || this.intentoEnvio());
  }

  /** Mensaje concreto, deducido del validador que falla. */
  mensajeError(campo: string): string {

    this.revision();

    const c = this.denunciaForm?.get(campo);
    if (!c || !c.errors) return '';

    const e = c.errors;
    const etiqueta = this.etiquetas[campo] ?? 'Este campo';

    if (e['required'])  return `${etiqueta} es obligatorio.`;
    if (e['pattern'])   return campo === 'cedula'
                               ? 'La cédula debe tener 10 dígitos.'
                               : 'El formato no es válido.';
    if (e['maxlength']) return `Máximo ${e['maxlength'].requiredLength} caracteres.`;

    return 'Revisa este campo.';
  }

  /** Lista de lo que falta, para el resumen de arriba. */
  camposFaltantes(): string[] {

    this.revision();

    if (!this.denunciaForm) return [];

    return Object.keys(this.etiquetas)
      .filter(campo => this.denunciaForm.get(campo)?.invalid)
      .map(campo => this.etiquetas[campo]);
  }

  /** Cuantos caracteres quedan en la narracion. */
  restanNarracion(): number {
    this.revision();
    return 500 - ((this.denunciaForm?.get('narracion')?.value ?? '') as string).length;
  }


  @ViewChild('map', { static: false })
  mapElement!: ElementRef<HTMLDivElement>;

  private map?: L.Map;
  private marker?: L.Marker;

  /** Estado del buscador de direcciones. */
  textoBusqueda = '';
  resultadosBusqueda: any[] = [];
  buscandoDireccion = false;

  denunciaForm!: FormGroup;

  cargando: boolean = false;

  rutasImagenes: File[] = [];

  /**
   * Miniatura por cada archivo elegido, en el mismo orden que rutasImagenes.
   *
   * La entrada se agrega al elegir el archivo y la miniatura se rellena cuando
   * FileReader termina. Antes se hacia al reves, dentro del onload: como la
   * lectura es asincrona, con varias imagenes el orden podia invertirse y
   * eliminar una borraba la de al lado.
   */
  previewsImagenes: { url: string; nombre: string; peso: string }[] = [];

  /** Tope por denuncia; el mismo numero se muestra en la zona de carga. */
  readonly maxImagenes = 5;

  /** 5 MB por imagen: por encima de eso el envio se vuelve lento sin ganar nada. */
  private readonly maxBytesImagen = 5 * 1024 * 1024;

  /** Resalta la zona mientras se arrastra un archivo encima. */
  arrastrandoImagen = false;

  /** 0-100 mientras se leen o se suben imagenes; null cuando no hay nada en curso. */
  progreso: number | null = null;
  mensajeProgreso = '';

  latitudSeleccionada: number | null = null;
  longitudSeleccionada: number | null = null;

  adminZonales: any[] = [];
  parroquias: any[] = [];
  barrios: any[] = [];

  tiposDenunciante: any[] = [];
  dependencias: any[] = [];
  predios: any[] = [];

  codigoPreview: string = 'UBA-000';

  // Viene del catalogo "Especies (plagas)": lo que se agregue alli aparece aqui.
  especies: any[] = [];



  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private ngZone: NgZone
  ) { }

  ngOnInit(): void {

    this.crearFormulario();

    this.cargarAdminZonal();
    this.cargarTipoDenunciante();
    this.cargarDependencia();
    this.cargarPredio();
    this.cargarEspecies();

    this.generarCodigoPreview();

    // =========================
    // EVENTOS REACTIVOS
    // =========================

    this.denunciaForm.get('adminZonal')
      ?.valueChanges.subscribe(() => {

        this.cargarParroquia();
      });

    this.denunciaForm.get('parroquia')
      ?.valueChanges.subscribe(() => {

        this.cargarBarrio();
      });

    this.denunciaForm.get('modoMapa')
      ?.valueChanges.subscribe(() => {

        this.actualizarModoMapa();

      });

    this.denunciaForm.valueChanges.subscribe(() => {

      this.validarFormulario();
    });

  }

  // =========================
  // FORMULARIO
  // =========================

  crearFormulario(): void {

    this.denunciaForm = this.fb.group({

      nombre: [
        '',
        Validators.required
      ],

      apellido: [
        '',
        Validators.required
      ],

      cedula: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{10}$/)
        ]
      ],

      direccion: [
        '',
        Validators.required
      ],

      referencia: [
        '',
        Validators.required
      ],

      narracion: [
        '',
        [
          Validators.required,
          Validators.maxLength(500)
        ]
      ],

      adminZonal: [
        '',
        Validators.required
      ],

      parroquia: [
        '',
        Validators.required
      ],

      barrio: [
        '',
        Validators.required
      ],

      tipoDenunciante: [
        '',
        Validators.required
      ],

      // Sin validador de inicio: se activa solo si el tipo es municipal.
      dependencia: [''],

      predio: [
        '',
        Validators.required
      ],

      latitud: [''],
      longitud: [''],

      modoMapa: ['Mapa'],

      especiesSeleccionadas: this.fb.array([])

    });

    this.denunciaForm.events.subscribe(() => this.revision.update(v => v + 1));

    // La dependencia solo aplica si el denunciante es una entidad municipal.
    // Exigirla siempre bloqueaba el formulario a cualquier ciudadano, y ademas
    // su catalogo estaba vacio: no habia forma de completarlo.
    this.denunciaForm.get('tipoDenunciante')!.valueChanges.subscribe(valor => {

      const dependencia = this.denunciaForm.get('dependencia')!;
      const esMunicipal = this.esDependienteMunicipal(valor);

      dependencia.setValidators(esMunicipal ? [Validators.required] : []);
      if (!esMunicipal) dependencia.setValue('');
      dependencia.updateValueAndValidity();
    });
  }

  /** El tipo "DEPENDIENTE MUNICIPAL" es el unico que exige dependencia. */
  esDependienteMunicipal(idTipo: unknown): boolean {

    if (!idTipo) return false;

    const tipo = (this.tiposDenunciante ?? []).find(
      (t: any) => String(t.id ?? t.idTipo) === String(idTipo));

    return ((tipo as any)?.nombre ?? '').toUpperCase().includes('DEPENDIENTE');
  }

  /** True cuando hay que mostrar el desplegable de dependencia. */
  requiereDependencia(): boolean {
    this.revision();
    return this.esDependienteMunicipal(this.denunciaForm?.get('tipoDenunciante')?.value);
  }

  // =========================
  // GET FORM ARRAY
  // =========================

  get especiesSeleccionadas(): FormArray {

    return this.denunciaForm.get(
      'especiesSeleccionadas'
    ) as FormArray;

  }

  // =========================
  // GENERAR CÓDIGO
  // =========================

  generarCodigoPreview(): void {

    this.http.get<any>(
      `${environment.url_api}/tramite/codigo-preview`
    ).subscribe({

      next: (resp) => {

        this.codigoPreview =
          resp.codigo;

        this.cd.markForCheck();
      },

      error: () => {

        this.codigoPreview =
          'UBA-000';
      }

    });

  }

  // =========================
  // CARGAR ADMIN ZONAL
  // =========================

  cargarAdminZonal(): void {

    this.http.get<any[]>(
      `${environment.url_api}/admin-zonal`
    ).subscribe({

      next: (data) => {

        this.adminZonales = data;

        this.cd.markForCheck();
      },

      error: (err) => {

        console.error(err);
      }

    });

  }

  // =========================
  // CARGAR PARROQUIA
  // =========================

  cargarParroquia(): void {

    const admin =
      this.denunciaForm.get('adminZonal')
        ?.value;

    // Al quitar la zona, lo que dependia de ella deja de tener sentido.
    if (!admin) {
      this.parroquias = [];
      this.barrios = [];
      this.denunciaForm.patchValue({ parroquia: '', barrio: '' }, { emitEvent: false });
      this.cd.markForCheck();
      return;
    }

    this.http.get<any[]>(
      `${environment.url_api}/parroquia/${admin}`
    ).subscribe({

      next: (data) => {

        this.parroquias = data;

        this.barrios = [];

        this.denunciaForm.patchValue({

          parroquia: '',
          barrio: ''
        });

        this.cd.markForCheck();
      },

      error: (err) => {

        console.error(err);
      }

    });

  }

  // =========================
  // CARGAR BARRIOS
  // =========================

  cargarBarrio(): void {

    const parroquia =
      this.denunciaForm.get('parroquia')
        ?.value;

    if (!parroquia) {
      this.barrios = [];
      this.denunciaForm.patchValue({ barrio: '' }, { emitEvent: false });
      this.cd.markForCheck();
      return;
    }

    this.http.get<any[]>(
      `${environment.url_api}/barrio/${parroquia}`
    ).subscribe({

      next: (data) => {

        this.barrios = data;

        this.cd.markForCheck();
      },

      error: (err) => {

        console.error(err);
      }

    });

  }

  // =========================
  // CARGAR TIPO DENUNCIANTE
  // =========================

  cargarTipoDenunciante(): void {

    this.http.get<any[]>(
      `${environment.url_api}/tipo-denunciante`
    ).subscribe({

      next: (data) => {

        this.tiposDenunciante = data;

        this.cd.markForCheck();
      },

      error: (err) => {

        console.error(err);
      }

    });

  }

  // =========================
  // CARGAR DEPENDENCIA
  // =========================

  cargarDependencia(): void {

    this.http.get<any[]>(
      `${environment.url_api}/dependencia`
    ).subscribe({

      next: (data) => {

        this.dependencias = data;

        this.cd.markForCheck();
      },

      error: (err) => {

        console.error(err);
      }

    });

  }

  // =========================
  // CARGAR PREDIO
  // =========================

  cargarPredio(): void {

    this.http.get<any[]>(
      `${environment.url_api}/predio`
    ).subscribe({

      next: (data) => {

        this.predios = data;

        this.cd.markForCheck();
      },

      error: (err) => {

        console.error(err);
      }

    });

  }

  // =========================
  // CARGAR ESPECIES
  // =========================

  cargarEspecies(): void {

    this.http.get<any[]>(
      `${environment.url_api}/especie`
    ).subscribe({

      next: (data) => {

        this.especies = data;

        this.cd.markForCheck();
      },

      error: (err) => {

        console.error(err);
      }

    });

  }

  ngAfterViewInit(): void {
    if (this.denunciaForm.get('modoMapa')?.value === 'Mapa') {
      this.initMap();
    }
  }

  ngOnDestroy(): void {
    this.destroyMap();
  }

  private destroyMap(): void {
    if (this.map) {
      this.map.remove();
      this.map = undefined;
    }

    if (this.marker) {
      this.marker.remove();
      this.marker = undefined;
    }
  }

  private initMap(): void {
    if (!this.mapElement) {
      return;
    }

    this.destroyMap();

    this.map = L.map(this.mapElement.nativeElement, {
      // Centrado en Quito: abrir en una vista mundial obligaba a buscar el
      // pais a mano antes de poder señalar nada.
      center: [CENTRO_QUITO.lat, CENTRO_QUITO.lon],
      zoom: 12,
      scrollWheelZoom: true,
      doubleClickZoom: false
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);

    const lat = this.denunciaForm.get('latitud')?.value;
    const lng = this.denunciaForm.get('longitud')?.value;

    if (lat && lng) {
      this.seleccionarCoordenadas(Number(lat), Number(lng));
    } else if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const currentLat = position.coords.latitude;
          const currentLng = position.coords.longitude;
          this.seleccionarCoordenadas(currentLat, currentLng);
        },
        () => {
          this.map?.setView([0, 0], 2);
        },
        { enableHighAccuracy: true, timeout: 5000 }
      );
    } else {
      this.map.setView([0, 0], 2);
    }

    this.map.on('click', (event: L.LeafletMouseEvent) => {
      this.seleccionarCoordenadas(event.latlng.lat, event.latlng.lng);
    });
  }

  // =========================
  // VALIDAR MODO MAPA
  // =========================

  actualizarModoMapa(): void {

    const modo =
      this.denunciaForm.get('modoMapa')
        ?.value;

    if (modo === 'Coordenadas') {

      this.denunciaForm.get('latitud')
        ?.setValidators([
          Validators.required
        ]);

      this.denunciaForm.get('longitud')
        ?.setValidators([
          Validators.required
        ]);

    } else {

      this.denunciaForm.get('latitud')
        ?.clearValidators();

      this.denunciaForm.get('longitud')
        ?.clearValidators();

    }

    this.denunciaForm.get('latitud')
      ?.updateValueAndValidity();

    this.denunciaForm.get('longitud')
      ?.updateValueAndValidity();

  }

  // =========================
  // SELECCIONAR ESPECIES
  // =========================

  seleccionarEspecie(
    event: any,
    id: number
  ): void {

    if (event.target.checked) {

      this.especiesSeleccionadas.push(
        this.fb.control(id)
      );

    } else {

      const index =
        this.especiesSeleccionadas.controls
          .findIndex(
            x => x.value === id
          );

      this.especiesSeleccionadas.removeAt(index);

    }

  }

  // =========================
  // SELECCIONAR IMAGEN
  // =========================

  seleccionarImagen(event: any): void {

    this.agregarImagenes(event.target.files);

    // Sin esto no se puede volver a elegir el mismo archivo tras quitarlo.
    event.target.value = '';
  }

  // =========================
  // ARRASTRAR Y SOLTAR
  // =========================

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

  // =========================
  // AGREGAR IMAGENES
  // =========================

  private agregarImagenes(archivos: FileList | null): void {

    if (!archivos || archivos.length === 0) {
      return;
    }

    const aceptados: File[] = [];

    for (let i = 0; i < archivos.length; i++) {

      const archivo = archivos[i];

      if (this.rutasImagenes.length + aceptados.length >= this.maxImagenes) {
        this.aviso.aviso(
          `Solo se permiten ${this.maxImagenes} imágenes por denuncia.`);
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

      // La miniatura se reserva ya, en el mismo indice que el archivo: la
      // lectura es asincrona y el orden de llegada no es el de seleccion.
      const entrada = {
        url: '',
        nombre: archivo.name,
        peso: this.pesoLegible(archivo.size)
      };

      this.rutasImagenes.push(archivo);
      this.previewsImagenes.push(entrada);

      const lector = new FileReader();

      lector.onload = () => {

        entrada.url = lector.result as string;

        leidas++;
        this.progreso = Math.round((leidas / aceptados.length) * 100);

        if (leidas === aceptados.length) {
          this.cerrarProgreso(inicio);
        }

        this.cd.markForCheck();
      };

      lector.onerror = () => {

        this.aviso.aviso(`No se pudo leer "${archivo.name}".`);

        this.eliminarImagen(this.previewsImagenes.indexOf(entrada));

        leidas++;
        if (leidas === aceptados.length) {
          this.cerrarProgreso(inicio);
        }

        this.cd.markForCheck();
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

    if (bytes < 1024) {
      return `${bytes} B`;
    }

    if (bytes < 1024 * 1024) {
      return `${Math.round(bytes / 1024)} KB`;
    }

    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  // =========================
  // ELIMINAR IMAGEN
  // =========================

  eliminarImagen(index: number): void {

    if (index < 0) {
      return;
    }

    this.rutasImagenes.splice(index, 1);

    this.previewsImagenes.splice(index, 1);

    this.cd.markForCheck();
  }

  // =========================
  // SELECCIONAR COORDENADAS
  // =========================

  seleccionarCoordenadas(
    lat: number,
    lng: number
  ): void {

    this.latitudSeleccionada = lat;
    this.longitudSeleccionada = lng;

    this.denunciaForm.patchValue({

      latitud: lat,
      longitud: lng
    });

    if (this.map) {
      if (this.marker) {
        this.marker.setLatLng([lat, lng]);
      } else {
        // Arrastrable: pulsar en el mapa deja el punto aproximado, y arrastrar
        // permite afinarlo sin tener que acertar al clic.
        this.marker = L.marker([lat, lng], { draggable: true, autoPan: true })
          .addTo(this.map);

        this.marker.on('dragend', () => {
          const p = this.marker!.getLatLng();
          this.latitudSeleccionada = p.lat;
          this.longitudSeleccionada = p.lng;
          this.denunciaForm.patchValue({ latitud: p.lat, longitud: p.lng });
          this.cd.markForCheck();
        });

        this.marker.bindTooltip('Arrástrame para ajustar el punto',
                                { direction: 'top', offset: [0, -38] });
      }

      // Solo se acerca si aun se esta lejos: reencuadrar en cada arrastre
      // pelearia con el usuario mientras coloca el marcador.
      if ((this.map.getZoom() ?? 0) < 15) {
        this.map.setView([lat, lng], 16);
      }
    }

    this.cd.markForCheck();
  }

  // =========================
  // VALIDAR FORMULARIO
  // =========================

  validarFormulario(): boolean {

    const especiesValidas =
      this.especiesSeleccionadas.length > 0;

    return (
      this.denunciaForm.valid &&
      especiesValidas
    );

  }

  // =========================
  // GUARDAR DENUNCIA
  // =========================

  guardarDenuncia(): void {

    // A partir del primer intento los errores se muestran aunque el usuario no
    // haya tocado el campo: antes solo decia "complete los campos" sin senalar
    // cuales, y en un formulario de 13 campos eso no ayuda a nadie.
    this.intentoEnvio.set(true);
    this.denunciaForm.markAllAsTouched();
    this.revision.update(v => v + 1);

    if (!this.validarFormulario()) {

      const faltan = this.camposFaltantes();

      this.aviso.aviso(
        faltan.length === 1
          ? `Falta: ${faltan[0]}`
          : `Faltan ${faltan.length} campos: ${faltan.slice(0, 3).join(', ')}`
            + (faltan.length > 3 ? '…' : ''));

      // Lleva la vista al primer campo con problema.
      const primero = document.querySelector('.campo-invalido');
      primero?.scrollIntoView({ behavior: 'smooth', block: 'center' });

      return;
    }

    this.cargando = true;

    const body = {

      nombre:
        this.denunciaForm.value.nombre,

      apellido:
        this.denunciaForm.value.apellido,

      cedula:
        this.denunciaForm.value.cedula,

      direccion:
        this.denunciaForm.value.direccion,

      referencia:
        this.denunciaForm.value.referencia,

      narracion:
        this.denunciaForm.value.narracion,

      admin_zonal:
        this.denunciaForm.value.adminZonal,

      parroquia:
        this.denunciaForm.value.parroquia,

      barrio:
        this.denunciaForm.value.barrio,

      tipo_denunciante:
        this.denunciaForm.value.tipoDenunciante,

      dependencia:
        this.denunciaForm.value.dependencia,

      predio:
        this.denunciaForm.value.predio,

      latitud:
        this.denunciaForm.value.latitud,

      longitud:
        this.denunciaForm.value.longitud,

      especies:
        this.especiesSeleccionadas.value
    };

    const formData =
      new FormData();

    formData.append(
      'datos',
      JSON.stringify(body)
    );

    this.rutasImagenes.forEach((img) => {

      formData.append(
        'imagenes',
        img
      );

    });

    // Con imagenes el envio puede tardar: se pide el progreso real de subida
    // en vez de dejar al ciudadano mirando un boton que dice "GUARDANDO...".
    this.progreso = 0;
    this.mensajeProgreso = this.rutasImagenes.length > 0
        ? 'Subiendo imágenes...'
        : 'Enviando denuncia...';

    this.http.post(
      `${environment.url_api}/denuncia`,
      formData,
      { reportProgress: true, observe: 'events' }
    ).subscribe({

      next: (evento: any) => {

        if (evento.type === HttpEventType.UploadProgress) {

          this.progreso = evento.total
              ? Math.round((evento.loaded / evento.total) * 100)
              : null;

          if (this.progreso === 100) {
            this.mensajeProgreso = 'Procesando en el servidor...';
          }

          this.cd.markForCheck();
          return;
        }

        if (evento.type !== HttpEventType.Response) {
          return;
        }

        const resp: any = evento.body;

        this.cargando = false;
        this.progreso = null;
        this.mensajeProgreso = '';

        // Dialogo, no toast: el codigo es lo unico con lo que el ciudadano
        // puede dar seguimiento, asi que debe acusarlo antes de que desaparezca.
        this.aviso.exitoDialogo(
          'Denuncia registrada',
          `Guarda este código para dar seguimiento: ${resp.codigo}`
        );

        this.denunciaForm.reset();

        this.rutasImagenes = [];

        this.previewsImagenes = [];

        this.especiesSeleccionadas.clear();

        this.generarCodigoPreview();

        this.cd.markForCheck();
      },

      error: (err) => {

        this.cargando = false;
        this.progreso = null;
        this.mensajeProgreso = '';

        console.error(err);

        this.aviso.aviso('Error al guardar denuncia');

        this.cd.markForCheck();
      }

    });

  }

  // =========================
  // VOLVER
  // =========================

  /**
   * Ayuda de la pantalla.
   *
   * Quien denuncia suele entrar una sola vez y no conoce el tramite: conviene
   * que pueda resolver la duda sin llamar a nadie.
   */
  mostrarAyuda(): void {

    this.aviso.ayuda('Cómo registrar una denuncia', `
      <ol class="lista-ayuda">
        <li><b>Tus datos.</b> Sirven para contactarte si el técnico necesita
            entrar al predio o confirmar algo. No se publican.</li>
        <li><b>Dónde es.</b> Elige administración zonal, parroquia y barrio; las
            listas se van filtrando entre ellas. Marca el punto exacto en el
            mapa: puedes buscar la calle y luego <b>arrastrar el marcador</b>.</li>
        <li><b>Qué viste.</b> Marca las especies e indica el tipo de predio.
            Si tienes fotos, adjúntalas: es lo que más ayuda al técnico a
            preparar la visita.</li>
        <li><b>Narración.</b> Cuenta desde cuándo ocurre y en qué parte del
            lugar. Los detalles concretos valen más que la extensión.</li>
        <li><b>Código de seguimiento.</b> Al enviar recibirás un código como
            <b>UBA-001</b>. Guárdalo: con él consultas en qué va tu denuncia.</li>
      </ol>
      <p class="nota-ayuda">Los campos con <b>*</b> son obligatorios. Si falta
         alguno, al enviar se listan arriba y se marcan en rojo.</p>
    `);
  }

  volver(): void {

    this.router.navigate([
      '/usuario'
    ]);

  }


  // ══════════════════════════════════════════════════════════════════
  // BUSCADOR DE DIRECCIONES
  // ══════════════════════════════════════════════════════════════════

  /**
   * Busca una direccion y mueve el mapa.
   *
   * Se usa Nominatim (OpenStreetMap): no necesita clave y basta para ubicar una
   * calle. La busqueda se limita a Ecuador para que "La Y" o "El Recreo" no
   * devuelvan resultados de medio mundo.
   *
   * Es un servicio externo con limite de uso: por eso se busca al pulsar y no
   * mientras se escribe.
   */
  buscarDireccion(): void {

    const texto = (this.textoBusqueda ?? '').trim();

    if (texto.length < 3) {
      this.aviso.aviso('Escribe al menos 3 caracteres para buscar.');
      return;
    }

    this.buscandoDireccion = true;
    this.resultadosBusqueda = [];
    this.cd.markForCheck();

    const url = 'https://nominatim.openstreetmap.org/search'
      + '?format=json&limit=5&countrycodes=ec&addressdetails=1'
      + '&q=' + encodeURIComponent(texto);

    this.http.get<any[]>(url).subscribe({

      next: (resultados) => {
        this.buscandoDireccion = false;
        this.resultadosBusqueda = resultados ?? [];

        if (this.resultadosBusqueda.length === 0) {
          this.aviso.aviso('No se encontró esa dirección. Prueba con otra referencia.');
        } else if (this.resultadosBusqueda.length === 1) {
          // Un solo resultado: no tiene sentido hacer elegir.
          this.elegirResultado(this.resultadosBusqueda[0]);
        }

        this.cd.markForCheck();
      },

      error: () => {
        this.buscandoDireccion = false;
        this.aviso.error('No se pudo buscar la dirección. Marca el punto en el mapa.');
        this.cd.markForCheck();
      }
    });
  }

  /** Lleva el mapa al resultado elegido y planta el marcador. */
  elegirResultado(resultado: any): void {

    const lat = Number(resultado.lat);
    const lon = Number(resultado.lon);

    if (!isFinite(lat) || !isFinite(lon)) return;

    this.map?.setView([lat, lon], 17);
    this.seleccionarCoordenadas(lat, lon);

    this.resultadosBusqueda = [];
    this.textoBusqueda = this.nombreCorto(resultado);
    this.cd.markForCheck();
  }

  /** Nominatim devuelve nombres muy largos; se recorta a lo util. */
  nombreCorto(resultado: any): string {
    return (resultado.display_name ?? '').split(',').slice(0, 3).join(',').trim();
  }

  limpiarBusqueda(): void {
    this.textoBusqueda = '';
    this.resultadosBusqueda = [];
    this.cd.markForCheck();
  }

  /** Centra el mapa donde esta el usuario, si el navegador lo permite. */
  usarMiUbicacion(): void {

    if (!navigator.geolocation) {
      this.aviso.aviso('Tu navegador no permite obtener la ubicación.');
      return;
    }

    this.buscandoDireccion = true;
    this.cd.markForCheck();

    navigator.geolocation.getCurrentPosition(
      (posicion) => {
        this.buscandoDireccion = false;
        const { latitude, longitude } = posicion.coords;
        this.map?.setView([latitude, longitude], 17);
        this.seleccionarCoordenadas(latitude, longitude);
        this.cd.markForCheck();
      },
      () => {
        this.buscandoDireccion = false;
        this.aviso.aviso('No se pudo obtener tu ubicación. Marca el punto en el mapa.');
        this.cd.markForCheck();
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
  }
}
