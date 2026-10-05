import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, finalize, of, switchMap, tap } from 'rxjs';

import { NotificacionesService } from '../core/notificaciones.service';
import { PermisosService } from '../core/permisos.service';
import { FormatoInforme, InformeInspeccion, InformesApi } from '../core/informes.api';

/**
 * Informe tecnico de la inspeccion, con las secciones del modelo oficial UBA.
 *
 * La primera vez llega un borrador armado con los datos del tramite; lo que
 * es propio de cada caso (diagnostico, recomendaciones, fotos...) se completa
 * aqui. Tecnico y administracion lo descargan en Word, para retocarlo o
 * firmarlo, o en PDF.
 *
 * El formulario edita directamente el objeto del informe (ngModel): tiene
 * listas anidadas que cambian de tamaño y un FormGroup no aporta nada aqui.
 */
@Component({
  selector: 'app-informe-inspeccion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './informe-inspeccion.html',
  styleUrls: ['./informe-inspeccion.css'],
})
export class InformeInspeccionComponent implements OnInit {
  private readonly ruta = inject(ActivatedRoute);
  private readonly informesApi = inject(InformesApi);
  private readonly aviso = inject(NotificacionesService);
  private readonly permisos = inject(PermisosService);

  readonly informe = signal<InformeInspeccion | null>(null);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly descargando = signal<FormatoInforme | null>(null);
  readonly subiendo = signal(0);

  /** Un hueco con indicador por cada foto que se esta subiendo. */
  readonly subidasEnCurso = computed(() => Array.from({ length: this.subiendo() }));

  /** Hay cambios sin guardar: descargar primero guarda. */
  readonly sucio = signal(false);

  /** Quien solo consulta ve el formulario bloqueado, pero puede descargar. */
  readonly puedeEditar = computed(() => this.permisos.puede('inspecciones', 'CREAR'));

  readonly maxFotos = 20;

  /** Los del modelo oficial; un clic los agrega a la lista. */
  readonly factoresSugeridos = [
    'Mal manejo de basura',
    'Áreas verdes sin tratamiento',
    'Condiciones estructurales inadecuadas',
    'Acumulación de agua estancada',
    'Almacenamiento inadecuado de materiales',
  ];

  readonly entidadesSugeridas = [
    'Administración Zonal',
    'EMASEO EP',
    'EPMMOP',
    'EPMAPS',
    'Ciudadanía',
    'Comerciantes',
  ];

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.cargar(id);
  }

  private cargar(id: number): void {
    this.cargando.set(true);
    this.informesApi.obtener(id).subscribe({
      next: (inf) => {
        this.informe.set(this.normalizar(inf));
        this.sucio.set(false);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        this.aviso.errorHttp(e, 'No se pudo cargar el informe.');
      },
    });
  }

  /** El input datetime-local no acepta segundos en todos los navegadores. */
  private normalizar(inf: InformeInspeccion): InformeInspeccion {
    return {
      ...inf,
      fechaInspeccion: inf.fechaInspeccion ? inf.fechaInspeccion.substring(0, 16) : null,
      recomendaciones: inf.recomendaciones.map((g) => ({ ...g, items: [...g.items] })),
    };
  }

  cambio(): void {
    this.sucio.set(true);
  }

  // ════════════════════════════════
  // GUARDAR Y DESCARGAR
  // ════════════════════════════════

  guardar(): void {
    this.guardarCambios().subscribe({
      next: () => this.aviso.exito('Informe guardado.'),
      error: (e) => this.aviso.errorHttp(e, 'No se pudo guardar el informe.'),
    });
  }

  /** Llamado solo con el informe ya cargado (los botones no existen antes). */
  private guardarCambios(): Observable<InformeInspeccion> {
    this.guardando.set(true);

    return this.informesApi.guardar(this.informe()!).pipe(
      tap((guardado) => {
        this.informe.set(this.normalizar(guardado));
        this.sucio.set(false);
      }),
      finalize(() => this.guardando.set(false)),
    );
  }

  /**
   * El archivo se arma con lo guardado en el servidor: si hay cambios
   * pendientes (o es un borrador que nunca se guardo) se guardan antes.
   */
  descargar(formato: FormatoInforme): void {
    const inf = this.informe();
    if (!inf) return;

    const debeGuardar = this.puedeEditar() && (this.sucio() || !inf.guardado);

    this.descargando.set(formato);

    (debeGuardar ? this.guardarCambios() : of(inf))
      .pipe(switchMap(() => this.informesApi.descargar(inf.idCodigoInterno, formato)))
      .subscribe({
        next: ({ archivo, nombre }) => {
          this.descargando.set(null);
          const url = URL.createObjectURL(archivo);
          const enlace = document.createElement('a');
          enlace.href = url;
          enlace.download = nombre;
          enlace.click();
          setTimeout(() => URL.revokeObjectURL(url), 1000);
        },
        error: (e) => {
          this.descargando.set(null);
          this.aviso.errorHttp(e, 'No se pudo generar el informe.');
        },
      });
  }

  volver(): void {
    window.history.back();
  }

  // ════════════════════════════════
  // LISTAS
  // ════════════════════════════════

  agregarParticipante(): void {
    this.informe()!.participantes.push({ nombre: '', cargo: '', institucion: 'UBA - MDMQ' });
    this.cambio();
  }

  agregarEspecie(): void {
    this.informe()!.detalleEspecies.push({ especie: '', nivel: '', indicios: '' });
    this.cambio();
  }

  quitar<T>(lista: T[], i: number): void {
    lista.splice(i, 1);
    this.cambio();
  }

  mover<T>(lista: T[], i: number, paso: -1 | 1): void {
    const j = i + paso;
    if (j < 0 || j >= lista.length) return;
    [lista[i], lista[j]] = [lista[j], lista[i]];
    this.cambio();
  }

  agregarFactor(factor: string): void {
    const inf = this.informe()!;
    const actuales = (inf.factoresRiesgo ?? '')
      .replace(/\.\s*$/, '')
      .split(',')
      .map((f) => f.trim())
      .filter((f) => f);

    if (actuales.some((f) => f.toLowerCase() === factor.toLowerCase())) return;

    inf.factoresRiesgo = [...actuales, factor].join(', ') + '.';
    this.cambio();
  }

  agregarGrupo(): void {
    const usadas = new Set(this.informe()!.recomendaciones.map((g) => g.entidad));
    const siguiente = this.entidadesSugeridas.find((e) => !usadas.has(e)) ?? '';
    this.informe()!.recomendaciones.push({ entidad: siguiente, items: [''] });
    this.cambio();
  }

  agregarItem(items: string[]): void {
    items.push('');
    this.cambio();
  }

  // ════════════════════════════════
  // FOTOS
  // ════════════════════════════════

  seleccionarFotos(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivos = Array.from(input.files ?? []);
    input.value = '';

    const inf = this.informe()!;
    const libres = this.maxFotos - inf.fotos.length;

    if (archivos.length > libres) {
      this.aviso.aviso(
        `El anexo admite ${this.maxFotos} fotos; se subirán ${Math.max(libres, 0)}.`,
      );
    }

    for (const archivo of archivos.slice(0, Math.max(libres, 0))) {
      // WEBP y GIF no se pueden incrustar en el Word ni en el PDF.
      if (!['image/jpeg', 'image/png'].includes(archivo.type)) {
        this.aviso.aviso(`"${archivo.name}": usa fotos JPG o PNG.`);
        continue;
      }

      this.subiendo.update((n) => n + 1);

      this.informesApi.subirFoto(archivo).subscribe({
        next: (r) => {
          inf.fotos.push({ ruta: r.ruta, url: r.url, descripcion: '' });
          this.subiendo.update((n) => n - 1);
          this.cambio();
        },
        error: (e) => {
          this.subiendo.update((n) => n - 1);
          this.aviso.errorHttp(e, `No se pudo subir "${archivo.name}".`);
        },
      });
    }
  }
}
