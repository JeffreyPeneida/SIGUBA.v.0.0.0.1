import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../environments/environment';
import { ApiService } from './api.service';

export interface ParticipanteInforme {
  nombre: string | null;
  cargo: string | null;
  institucion: string | null;
}

export interface DetalleEspecieInforme {
  especie: string | null;
  nivel: string | null;
  indicios: string | null;
}

/** Recomendaciones dirigidas a una entidad (EMASEO EP, Ciudadanía...). */
export interface GrupoRecomendacion {
  entidad: string;
  items: string[];
}

export interface FotoInforme {
  /** Ruta en el bucket: lo que se guarda. */
  ruta: string;
  /** URL firmada para mostrarla; caduca. */
  url?: string | null;
  descripcion: string | null;
}

/** Informe de inspeccion (DtoInformeInspeccion), secciones del modelo oficial UBA. */
export interface InformeInspeccion {
  idCodigoInterno: number;
  codigoTramite: string | null;
  /** false mientras sea el borrador armado con los datos del tramite. */
  guardado: boolean;
  fechaActualizacion: string | null;
  actualizadoPor: string | null;

  documentoAtendido: string | null;
  tipoInspeccion: string | null;
  fechaInspeccion: string | null;
  plagas: string | null;
  nivel: string | null;
  asunto: string | null;

  direccion: string | null;
  sectorBarrio: string | null;
  adminZonal: string | null;
  tipoLugar: string | null;
  areasSupervisadas: string | null;
  coordenadas: string | null;
  beneficiarios: string | null;
  personaContactada: string | null;
  participantes: ParticipanteInforme[];

  detalleEspecies: DetalleEspecieInforme[];

  factoresRiesgo: string | null;
  diagnostico: string | null;

  tipoControl: 'ACTIVO' | 'PASIVO' | null;
  programaActuacion: string | null;

  conclusiones: string | null;
  recomendaciones: GrupoRecomendacion[];

  elaboradoNombre: string | null;
  elaboradoCargo: string | null;
  revisadoNombre: string | null;
  revisadoCargo: string | null;
  fechaElaboracion: string | null;

  fotos: FotoInforme[];
}

export type FormatoInforme = 'pdf' | 'word';

@Injectable({ providedIn: 'root' })
export class InformesApi {
  private readonly api = inject(ApiService);

  // Descargas y subidas necesitan opciones (blob, multipart) que ApiService no expone.
  private readonly http = inject(HttpClient);
  private readonly base = environment.url_api;

  obtener(idCodigoInterno: number): Observable<InformeInspeccion> {
    return this.api.get<InformeInspeccion>(`informe-inspeccion/${idCodigoInterno}`);
  }

  guardar(informe: InformeInspeccion): Observable<InformeInspeccion> {
    return this.api.put<InformeInspeccion>(
      `informe-inspeccion/${informe.idCodigoInterno}`,
      informe,
    );
  }

  /** Descarga el informe y devuelve el archivo con el nombre que propone el backend. */
  descargar(
    idCodigoInterno: number,
    formato: FormatoInforme,
  ): Observable<{ archivo: Blob; nombre: string }> {
    return this.http
      .get(`${this.base}/informe-inspeccion/${idCodigoInterno}/${formato}`, {
        responseType: 'blob',
        observe: 'response',
      })
      .pipe(
        map((r: HttpResponse<Blob>) => ({
          archivo: r.body!,
          nombre: this.nombreArchivo(r, idCodigoInterno, formato),
        })),
      );
  }

  /** Sube una foto del anexo; la respuesta trae la ruta a guardar y la URL para verla. */
  subirFoto(archivo: File): Observable<{ ruta: string; url: string }> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<{ ruta: string; url: string }>(
      `${this.base}/archivos/inspecciones`,
      datos,
    );
  }

  private nombreArchivo(r: HttpResponse<Blob>, id: number, formato: FormatoInforme): string {
    const cabecera = r.headers.get('Content-Disposition') ?? '';

    // filename*=UTF-8''... tiene prioridad sobre filename="..."
    const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(cabecera);
    if (utf8) return decodeURIComponent(utf8[1]);

    const simple = /filename="?([^";]+)"?/i.exec(cabecera);
    if (simple) return simple[1];

    return `Informe_inspeccion_${id}.${formato === 'word' ? 'docx' : 'pdf'}`;
  }
}
