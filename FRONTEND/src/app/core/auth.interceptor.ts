import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

/**
 * Adjunta el JWT a cada llamada a la API y cierra la sesion si el backend
 * responde 401 (token caducado o invalido).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.token;

  // El propio login no lleva token.
  const peticion = token && !req.url.includes('/login')
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((e: HttpErrorResponse) => {

      // Solo se expulsa a quien TENIA sesion: un 401 con token significa que
      // caduco. Sin token, el 401 es lo esperado en una pantalla publica
      // (el registro consulta catalogos) y echar al login dejaba esa pantalla
      // inalcanzable: entraba y rebotaba sola.
      if (e.status === 401 && token && !req.url.includes('/login')) {
        auth.logout();
        router.navigate(['/login']);
      }

      return throwError(() => e);
    })
  );
};
