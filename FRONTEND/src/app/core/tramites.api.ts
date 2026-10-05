import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { Respuesta } from './usuarios.api';

export interface Referencia {
  [clave: string]: unknown;
}

/** Tramite tal y como lo devuelve el backend (DtoTramite). */
export interface Tramite {
  idCodigoInterno: number;
  idTramite?: string;
  fechaDenuncia?: string;
  narracion?: string;
  latitud?: number;
  longitud?: number;
  fechaInspeccion?: string;
  horaInspeccion?: string;
  fechaAsignacionTecnico?: string;
  descripcion?: string;
  conclusiones?: string;
  recomendaciones?: string;
  estado?: string;

  denunciante?: { idDenunciante?: number; nombre?: string; apellido?: string; cedula?: string } | null;
  tecnico?: { idTecnico?: number; nombre?: string; apellido?: string } | null;
  ubicacion?: { idUbicacion?: number; direccion?: string; referencia?: string } | null;
  areaInspeccion?: { idArea?: number; nombre?: string } | null;
  motivoInspeccion?: { idMotivo?: number; nombre?: string } | null;
  barrio?: { idBarrio?: number; nombre?: string } | null;
}

/** Datos que espera PUT /api/tramite/actualizarInspeccion. */
export interface ActualizacionInspeccion {
  idCodigoInterno: number;
  fechaInspeccion: string;
  horaInspeccion: string;
  idArea: number;
  idMotivo: number;
  descripcion: string;
  conclusiones: string;
  recomendaciones: string;
  idRegistra: number;
}

/** Tecnico de UBA_TECNICO con su carga de trabajo. */
export interface TecnicoCarga {
  idTecnico: number;
  nombre: string;
  apellido: string;
  cedula?: string;
  correo?: string;
  pendientes: number;
  total: number;
}

/** Ficha completa de un tramite, tal y como la aplana el backend. */
export interface DetalleTramite {
  id_codigo_interno: number;
  id_tramite?: string;
  fecha_denuncia?: string;
  narracion?: string;
  latitud?: number;
  longitud?: number;
  fecha_inspeccion?: string;
  descripcion?: string;
  conclusiones?: string;
  recomendaciones?: string;
  estado?: string;
  nombre?: string;
  apellido?: string;
  cedula?: string;
  telefono?: string;
  tipo_denunciante?: string;
  id_tecnico?: number;
  tecnico?: string;
  direccion?: string;
  referencia?: string;
  admin_zonal?: string;
  parroquia?: string;
  barrio?: string;
  predio?: string;
  dependencia?: string;
  area?: string;
  motivo?: string;
  especies?: string[];
}

export interface ImagenTramite {
  ruta_imagen: string;
  ruta: string;
}

/** Un solo sitio donde viven las rutas de tramites del backend. */
@Injectable({ providedIn: 'root' })
export class TramitesApi {

  private readonly api = inject(ApiService);

  todos(): Observable<Tramite[]> {
    return this.api.get<Tramite[]>('tramite/buscarTramites');
  }

  /** Sin fecha de inspeccion todavia. */
  denuncias(): Observable<Tramite[]> {
    return this.api.get<Tramite[]>('tramite/buscarDenuncias');
  }

  /** Ya inspeccionados. */
  inspecciones(): Observable<Tramite[]> {
    return this.api.get<Tramite[]>('tramite/buscarInspecciones');
  }

  /** Las del usuario con sesion: las que registro y las hechas con su cedula. */
  misDenuncias(): Observable<Tramite[]> {
    return this.api.get<Tramite[]>('tramite/misDenuncias');
  }

  sinTecnico(): Observable<Tramite[]> {
    return this.api.get<Tramite[]>('tramite/buscarSinTecnico');
  }

  porTecnico(idTecnico: number): Observable<Tramite[]> {
    return this.api.get<Tramite[]>(`tramite/buscarPorTecnico/${idTecnico}`);
  }

  /**
   * Lo asignado a quien tiene la sesion abierta.
   *
   * El identificador de tecnico no viaja desde aqui: lo resuelve el backend a
   * partir del token, que es el unico dato que el navegador no puede falsear.
   */
  misAsignados(): Observable<Tramite[]> {
    return this.api.get<Tramite[]>('tramite/misAsignados');
  }

  /** Ficha completa para la pantalla de seguimiento. */
  detalle(id: number): Observable<DetalleTramite> {
    return this.api.get<DetalleTramite>(`tramite/${id}`);
  }

  imagenesDenuncia(id: number): Observable<ImagenTramite[]> {
    return this.api.get<ImagenTramite[]>(`imagen-denuncia/${id}`);
  }

  porInterno(id: number): Observable<Tramite> {
    return this.api.get<Tramite>(`tramite/buscarTramiteInterno/${id}`);
  }

  /**
   * Tecnicos disponibles con su carga: los usuarios con rol TECNICO activos,
   * devueltos por su ficha de UBA_TECNICO, que es a donde apunta la clave
   * foranea del tramite.
   */
  tecnicosDisponibles(): Observable<TecnicoCarga[]> {
    return this.api.get<TecnicoCarga[]>('tecnico');
  }

  asignarTecnico(idInterno: number, idTecnico: number): Observable<Respuesta> {
    return this.api.put<Respuesta>(
      `tramite/asignarTecnico?idInterno=${idInterno}&idTecnico=${idTecnico}`);
  }

  actualizarInspeccion(datos: ActualizacionInspeccion): Observable<Respuesta> {
    return this.api.put<Respuesta>('tramite/actualizarInspeccion', datos);
  }

  actualizarUbicacion(idInterno: number, lat: number, lon: number): Observable<Respuesta> {
    return this.api.put<Respuesta>(
      `tramite/actualizarUbicacion?idInterno=${idInterno}&latitud=${lat}&longitud=${lon}`);
  }

  eliminar(idInterno: number): Observable<Respuesta> {
    return this.api.put<Respuesta>(`tramite/eliminarTramite/${idInterno}`);
  }

  /**
   * Borrado definitivo: no deja rastro en la base y borra las fotos de MinIO.
   * Distinto de eliminar(), que solo marca el tramite como INACTIVO.
   */
  borrarDefinitivo(idInterno: number): Observable<Respuesta> {
    return this.api.delete<Respuesta>(`tramite/borrar/${idInterno}`);
  }

  restaurar(idInterno: number): Observable<Respuesta> {
    return this.api.put<Respuesta>(`tramite/restaurarTramite/${idInterno}`);
  }
}
