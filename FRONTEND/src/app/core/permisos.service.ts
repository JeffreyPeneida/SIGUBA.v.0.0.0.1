import { Injectable, computed, effect, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, shareReplay, tap } from 'rxjs';

import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Rol } from './auth.models';

export type Accion = 'VER' | 'CREAR' | 'EDITAR' | 'ELIMINAR';

/** Una pantalla y lo que el rol puede hacer en ella (GET /api/permisos/mios). */
export interface PermisoPantalla {
  clave: string;
  nombre: string;
  ruta: string;
  icono: string;
  seccion: string;
  orden: number;
  accionesDisponibles: string;
  /** Aparece en el menu lateral. Oculta sigue accesible por su ruta si hay permiso. */
  enMenu: boolean;
  /** Desactivada no da acceso a nadie. */
  activa: boolean;
  ver: boolean;
  crear: boolean;
  editar: boolean;
  eliminar: boolean;
}

/** Matriz completa para la pantalla de Perfiles (GET /api/permisos). */
export interface MatrizPermisos {
  acciones: Accion[];
  roles: Record<Rol, PermisoPantalla[]>;
}

/**
 * Permisos del usuario con sesion.
 *
 * Deciden el menu, las rutas y que botones se muestran. Antes eso eran listas
 * de roles escritas a mano en cada sitio. La puerta real sigue en el backend:
 * esconder un boton aqui solo evita ofrecer algo que el servidor rechazaria.
 */
@Injectable({ providedIn: 'root' })
export class PermisosService {

  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);

  private readonly _pantallas = signal<PermisoPantalla[] | null>(null);
  private carga$: Observable<boolean> | null = null;

  readonly pantallas = computed(() => this._pantallas() ?? []);
  readonly cargados = computed(() => this._pantallas() !== null);

  /** De quien son los permisos cargados. */
  private dueno: string | null = null;

  constructor() {
    // Otro usuario (o ninguno) invalida lo cargado. Se compara con el dueno
    // porque el efecto tambien corre al arrancar, y borraria lo que el guard
    // acabara de cargar.
    effect(() => {
      const actual = this.auth.usuario()?.usuario ?? null;
      if (actual !== this.dueno) this.limpiar();
    });
  }

  /** Carga los permisos si hacen falta; emite true cuando estan listos. */
  asegurar(): Observable<boolean> {

    if (!this.auth.autenticado()) return of(false);
    if (this._pantallas() !== null) return of(true);

    if (!this.carga$) {
      this.carga$ = this.api.get<PermisoPantalla[]>('permisos/mios').pipe(
        tap(p => {
          this.dueno = this.auth.usuario()?.usuario ?? null;
          this._pantallas.set(p ?? []);
        }),
        map(() => true),
        catchError(() => {
          this.carga$ = null;
          return of(false);
        }),
        shareReplay(1)
      );
    }
    return this.carga$;
  }

  /** Vuelve a pedirlos, tras editar los permisos del propio rol. */
  recargar(): Observable<boolean> {
    this.limpiar();
    return this.asegurar();
  }

  puede(pantalla: string, accion: Accion = 'VER'): boolean {
    const p = this.pantallas().find(x => x.clave === pantalla);
    if (!p) return false;
    return !!p[accion.toLowerCase() as 'ver' | 'crear' | 'editar' | 'eliminar'];
  }

  /**
   * Adonde entrar tras iniciar sesion: la pantalla habitual del rol si la
   * tiene permitida; si no, la primera que pueda ver.
   */
  rutaInicial(): string {

    const preferida: Record<string, string> = {
      ADMIN: 'inicio', TECNICO: 'inspecciones', USUARIO: 'mis-denuncias'
    };

    const rol = this.auth.rol();
    const clave = rol ? preferida[rol] : null;

    if (clave && this.puede(clave)) {
      return this.pantallas().find(p => p.clave === clave)!.ruta;
    }

    return this.pantallas().find(p => p.ver)?.ruta ?? '/login';
  }

  matriz(): Observable<MatrizPermisos> {
    return this.api.get<MatrizPermisos>('permisos');
  }

  /** Configuracion del menu lateral (Perfiles > Menu lateral). */
  pantallasMenu(): Observable<PermisoPantalla[]> {
    return this.api.get<PermisoPantalla[]>('permisos/pantallas');
  }

  guardarPantallas(filas: Partial<PermisoPantalla>[]): Observable<PermisoPantalla[]> {
    return this.api.put<PermisoPantalla[]>('permisos/pantallas', filas);
  }

  guardarRol(rol: Rol, filas: Partial<PermisoPantalla>[]): Observable<MatrizPermisos> {
    return this.api.put<MatrizPermisos>(`permisos/${rol}`, filas);
  }

  private limpiar(): void {
    this.dueno = null;
    this._pantallas.set(null);
    this.carga$ = null;
  }
}
