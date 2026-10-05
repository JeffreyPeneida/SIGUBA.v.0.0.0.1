import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { ApiService } from './api.service';
import { Rol, Sesion, UsuarioSesion } from './auth.models';

const CLAVE_SESION = 'siguba.sesion';

/**
 * Sesion del usuario autenticado.
 *
 * Guarda el JWT que emite el backend y lo expone para que el interceptor lo
 * adjunte a cada peticion. La autorizacion real la hace el servidor: aqui el
 * rol solo sirve para decidir que pantallas mostrar.
 *
 * Se usa sessionStorage: la sesion sobrevive a recargar la pagina pero no a
 * cerrar la pestana.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly api = inject(ApiService);

  private readonly _sesion = signal<Sesion | null>(this.leerSesion());

  readonly usuario = computed<UsuarioSesion | null>(() => this._sesion()?.usuario ?? null);
  readonly autenticado = computed(() => this._sesion() !== null);
  readonly rol = computed<Rol | null>(() => this._sesion()?.usuario?.rol ?? null);

  get token(): string | null {
    return this._sesion()?.token ?? null;
  }

  login(usuario: string, password: string): Observable<Sesion> {

    return this.api
      .post<Sesion>('login', { usuario, password })
      .pipe(tap(s => this.guardarSesion(s)));
  }

  logout(): void {
    this._sesion.set(null);
    try {
      sessionStorage.removeItem(CLAVE_SESION);
    } catch {
      // sessionStorage puede no estar disponible (modo privado, SSR).
    }
  }

  tieneRol(...roles: Rol[]): boolean {
    const actual = this.rol();
    return actual !== null && roles.includes(actual);
  }

  /** Pantalla inicial de cada rol tras iniciar sesion. */
  rutaInicial(): string {
    switch (this.rol()) {
      case 'ADMIN':   return '/gui';
      case 'TECNICO': return '/tecnico';
      case 'USUARIO': return '/usuario';
      default:        return '/login';
    }
  }

  private guardarSesion(s: Sesion): void {
    this._sesion.set(s);
    try {
      sessionStorage.setItem(CLAVE_SESION, JSON.stringify(s));
    } catch {
      // Si no se puede persistir, la sesion vive solo en memoria.
    }
  }

  private leerSesion(): Sesion | null {
    try {
      const crudo = sessionStorage.getItem(CLAVE_SESION);
      if (!crudo) return null;

      const s = JSON.parse(crudo) as Sesion;
      return s?.token && s?.usuario ? s : null;
    } catch {
      return null;
    }
  }
}
