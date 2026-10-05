import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

/**
 * Punto unico de salida HTTP. Concentra la URL base para que ningun componente
 * vuelva a incrustar hosts ni puertos: environment.url_api es '/api', relativo
 * al propio origen, y lo reenvia nginx (Docker) o proxy.conf.json (ng serve).
 */
@Injectable({ providedIn: 'root' })
export class ApiService {

  private readonly http = inject(HttpClient);
  private readonly base = environment.url_api;

  private url(ruta: string): string {
    return `${this.base}/${ruta.replace(/^\/+/, '')}`;
  }

  get<T>(ruta: string): Observable<T> {
    return this.http.get<T>(this.url(ruta));
  }

  post<T>(ruta: string, cuerpo: unknown): Observable<T> {
    return this.http.post<T>(this.url(ruta), cuerpo);
  }

  put<T>(ruta: string, cuerpo?: unknown): Observable<T> {
    return this.http.put<T>(this.url(ruta), cuerpo ?? {});
  }

  delete<T>(ruta: string): Observable<T> {
    return this.http.delete<T>(this.url(ruta));
  }
}
