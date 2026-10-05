import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { Rol } from './auth.models';

/** Usuario tal y como lo devuelve y espera el backend (DtoUsuario). */
export interface Usuario {
  idUsuario?: number;
  nombre: string;
  apellido: string;
  cedula: string;
  fechaNacimiento: string;
  usuario: string;
  mail?: string;
  password?: string;
  rol: Rol;
  estado?: string;
  adminZonal?: { idAdminZonal: number; nombre?: string } | null;
}

export interface Respuesta {
  codigo: number;
  mensaje: string;
  detalle?: string | null;
}

/** Un solo sitio donde viven las rutas de usuarios del backend. */
@Injectable({ providedIn: 'root' })
export class UsuariosApi {

  private readonly api = inject(ApiService);

  /** Con `incluirInactivos` trae tambien las cuentas desactivadas, para poder reactivarlas. */
  listar(incluirInactivos = false): Observable<Usuario[]> {
    return this.api.get<Usuario[]>(`usuarios${incluirInactivos ? '?incluirInactivos=true' : ''}`);
  }

  buscar(texto: string): Observable<Usuario[]> {
    return this.api.get<Usuario[]>(`buscarUsuario/${encodeURIComponent(texto)}`);
  }

  porRol(rol: Rol): Observable<Usuario[]> {
    return this.api.get<Usuario[]>(`usuariosRol/${rol}`);
  }

  porCedula(cedula: string): Observable<Usuario> {
    return this.api.get<Usuario>(`usuarioCedula/${encodeURIComponent(cedula)}`);
  }

  crear(usuario: Usuario): Observable<Respuesta> {
    return this.api.post<Respuesta>('crearUsuario', usuario);
  }

  actualizar(id: number, usuario: Usuario): Observable<Respuesta> {
    return this.api.put<Respuesta>(`actualizarUsuario/${id}`, usuario);
  }

  /** Desactiva: el backend marca ESTADO = 'INACTIVO' y la cuenta deja de entrar al instante. */
  eliminar(cedula: string): Observable<Respuesta> {
    return this.api.delete<Respuesta>(`eliminarUsuario/${encodeURIComponent(cedula)}`);
  }

  restaurar(cedula: string): Observable<Respuesta> {
    return this.api.put<Respuesta>(`restaurarUsuario/${encodeURIComponent(cedula)}`);
  }
}
