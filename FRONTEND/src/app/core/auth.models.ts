/** Roles definidos en el backend (enum RolUsuario). */
export type Rol = 'ADMIN' | 'TECNICO' | 'USUARIO';

/** Usuario devuelto por el backend. */
export interface UsuarioSesion {
  idUsuario: number;
  nombre: string;
  apellido: string;
  cedula: string;
  usuario: string;
  mail?: string;
  rol: Rol;
}

/** Respuesta de POST /api/login. */
export interface Sesion {
  token: string;
  expiraEnMs: number;
  usuario: UsuarioSesion;
}
