import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { Respuesta } from './usuarios.api';

/** Descripcion de un catalogo administrable, tal y como la publica el backend. */
export interface DefinicionCatalogo {
  clave: string;
  etiqueta: string;
  clavePadre?: string | null;
  etiquetaPadre?: string;
  tieneDescripcion: boolean;
}

/** Una fila de cualquier catalogo. */
export interface ItemCatalogo {
  id?: number;
  nombre: string;
  idPadre?: number | null;
  nombrePadre?: string;
  descripcion?: string;
}

/**
 * Acceso a los catalogos.
 *
 * Un solo servicio para los 13: comparten forma, asi que la clave del catalogo
 * viaja en la ruta en vez de tener un metodo por cada uno.
 */
@Injectable({ providedIn: 'root' })
export class CatalogosApi {

  private readonly api = inject(ApiService);

  definiciones(): Observable<DefinicionCatalogo[]> {
    return this.api.get<DefinicionCatalogo[]>('catalogos');
  }

  /** `idPadre` acota a las filas de un padre concreto (parroquias de una zona). */
  listar(clave: string, idPadre?: number | null): Observable<ItemCatalogo[]> {
    const filtro = idPadre != null ? `?padre=${idPadre}` : '';
    return this.api.get<ItemCatalogo[]>(`catalogos/${clave}${filtro}`);
  }

  crear(clave: string, item: ItemCatalogo): Observable<ItemCatalogo> {
    return this.api.post<ItemCatalogo>(`catalogos/${clave}`, item);
  }

  actualizar(clave: string, id: number, item: ItemCatalogo): Observable<ItemCatalogo> {
    return this.api.put<ItemCatalogo>(`catalogos/${clave}/${id}`, item);
  }

  borrar(clave: string, id: number): Observable<Respuesta> {
    return this.api.delete<Respuesta>(`catalogos/${clave}/${id}`);
  }
}
