import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';

import { AuthService } from './auth.service';
import { Rol } from './auth.models';
import { Accion, PermisosService } from './permisos.service';

/** Exige sesion iniciada; si no la hay, devuelve al login. */
export const authGuard: CanActivateFn = () => {

  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.autenticado() ? true : router.createUrlTree(['/login']);
};

/**
 * Exige permiso sobre una pantalla, segun lo configurado en Perfiles.
 * Sin permiso lleva a la pantalla inicial del usuario, no al login.
 */
export const permisoGuard = (pantalla: string, accion: Accion = 'VER'): CanActivateFn => () => {

  const auth = inject(AuthService);
  const permisos = inject(PermisosService);
  const router = inject(Router);

  if (!auth.autenticado()) {
    return router.createUrlTree(['/login']);
  }

  return permisos.asegurar().pipe(map(listos => {
    if (!listos) return router.createUrlTree(['/login']);
    if (permisos.puede(pantalla, accion)) return true;

    const destino = permisos.rutaInicial();
    // Si la inicial es esta misma (no deberia), evitar el bucle.
    return router.createUrlTree([destino === router.url ? '/login' : destino]);
  }));
};

/** Exige ademas uno de los roles indicados. */
export const rolGuard = (...roles: Rol[]): CanActivateFn => () => {

  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.autenticado()) {
    return router.createUrlTree(['/login']);
  }

  return auth.tieneRol(...roles)
    ? true
    : router.createUrlTree([auth.rutaInicial()]);
};

/** Evita volver al login con la sesion ya iniciada. */
export const invitadoGuard: CanActivateFn = () => {

  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.autenticado()
    ? router.createUrlTree([auth.rutaInicial()])
    : true;
};
