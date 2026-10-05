import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { Rol } from '../core/auth.models';
import { AuthService } from '../core/auth.service';
import { NotificacionesService } from '../core/notificaciones.service';
import { Accion, PermisoPantalla, PermisosService } from '../core/permisos.service';
import { MenuLateralEditorComponent } from './menu-lateral';

type Campo = 'ver' | 'crear' | 'editar' | 'eliminar';

interface InfoRol {
  rol: Rol;
  nombre: string;
  descripcion: string;
  icono: string;
}

/**
 * Perfiles y permisos: que pantallas ve cada rol y que puede hacer en ellas.
 *
 * Los roles son fijos (ADMIN, TECNICO, USUARIO); lo que se configura es su
 * acceso. Los cambios se guardan por rol y aplican en la siguiente peticion de
 * quien tenga ese rol, sin volver a iniciar sesion.
 */
@Component({
  selector: 'app-perfiles',
  standalone: true,
  templateUrl: './perfiles.html',
  imports: [MenuLateralEditorComponent]
})
export class PerfilesComponent implements OnInit {

  private readonly permisos = inject(PermisosService);
  private readonly auth = inject(AuthService);
  private readonly aviso = inject(NotificacionesService);

  readonly roles: InfoRol[] = [
    { rol: 'ADMIN',   nombre: 'Administrador', descripcion: 'Gestiona la operación y la configuración', icono: 'fa-user-shield' },
    { rol: 'TECNICO', nombre: 'Técnico',       descripcion: 'Atiende inspecciones en campo',            icono: 'fa-helmet-safety' },
    { rol: 'USUARIO', nombre: 'Usuario',       descripcion: 'Registra denuncias y consulta las suyas',  icono: 'fa-user' }
  ];

  readonly acciones: { campo: Campo; accion: Accion; texto: string; icono: string }[] = [
    { campo: 'ver',      accion: 'VER',      texto: 'Ver',      icono: 'fa-eye' },
    { campo: 'crear',    accion: 'CREAR',    texto: 'Crear',    icono: 'fa-plus' },
    { campo: 'editar',   accion: 'EDITAR',   texto: 'Editar',   icono: 'fa-pen' },
    { campo: 'eliminar', accion: 'ELIMINAR', texto: 'Eliminar', icono: 'fa-trash' }
  ];

  /** Pestana: permisos por rol o configuracion del menu lateral. */
  readonly pestana = signal<'permisos' | 'menu'>('permisos');

  readonly rolActivo = signal<Rol>('ADMIN');
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly puedeEditar = computed(() => this.permisos.puede('perfiles', 'EDITAR'));

  /** Lo guardado en el servidor, para saber si hay cambios y poder descartarlos. */
  private readonly original = signal<Record<string, PermisoPantalla[]>>({});

  /** Lo que se esta editando. */
  readonly borrador = signal<Record<string, PermisoPantalla[]>>({});

  readonly filas = computed(() => this.borrador()[this.rolActivo()] ?? []);

  /** Filas agrupadas por seccion del menu, en su orden. */
  readonly secciones = computed(() => {
    const grupos = new Map<string, PermisoPantalla[]>();
    for (const f of this.filas()) {
      grupos.set(f.seccion, [...(grupos.get(f.seccion) ?? []), f]);
    }
    return [...grupos].map(([titulo, pantallas]) => ({ titulo, pantallas }));
  });

  readonly hayCambios = computed(() => this.cambiosDe(this.rolActivo()) > 0);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.permisos.matriz().subscribe({
      next: m => { this.aplicar(m.roles); this.cargando.set(false); },
      error: e => {
        this.cargando.set(false);
        this.aviso.errorHttp(e, 'No se pudieron cargar los permisos.');
      }
    });
  }

  elegirRol(rol: Rol): void {
    this.rolActivo.set(rol);
  }

  disponible(p: PermisoPantalla, accion: Accion): boolean {
    return p.accionesDisponibles.split(',').includes(accion);
  }

  /** El ADMIN no puede perder Perfiles: nadie podria devolverle el acceso. */
  bloqueado(p: PermisoPantalla, campo: Campo): boolean {
    return this.rolActivo() === 'ADMIN' && p.clave === 'perfiles' && (campo === 'ver' || campo === 'editar');
  }

  alternar(p: PermisoPantalla, campo: Campo): void {

    if (!this.puedeEditar() || this.bloqueado(p, campo)) return;

    const rol = this.rolActivo();
    const valor = !p[campo];

    this.borrador.update(b => ({
      ...b,
      [rol]: b[rol].map(f => {
        if (f.clave !== p.clave) return f;
        const nueva = { ...f, [campo]: valor };
        // Sin ver no tiene sentido lo demas; y cualquier otra accion implica ver.
        if (campo === 'ver' && !valor) {
          nueva.crear = nueva.editar = nueva.eliminar = false;
        } else if (campo !== 'ver' && valor) {
          nueva.ver = true;
        }
        return nueva;
      })
    }));
  }

  /** Marca o desmarca todas las acciones disponibles de una pantalla. */
  alternarTodo(p: PermisoPantalla): void {

    if (!this.puedeEditar()) return;

    const todas = this.acciones.filter(a => this.disponible(p, a.accion));
    const encender = !todas.every(a => p[a.campo]);
    const rol = this.rolActivo();

    this.borrador.update(b => ({
      ...b,
      [rol]: b[rol].map(f => {
        if (f.clave !== p.clave) return f;
        const nueva = { ...f };
        for (const a of todas) {
          if (!this.bloqueado(f, a.campo)) nueva[a.campo] = encender;
        }
        return nueva;
      })
    }));
  }

  cambiosDe(rol: string): number {
    const antes = this.original()[rol] ?? [];
    return (this.borrador()[rol] ?? []).filter(f => {
      const o = antes.find(x => x.clave === f.clave);
      return !o || this.acciones.some(a => o[a.campo] !== f[a.campo]);
    }).length;
  }

  cuantasVe(rol: string): number {
    return (this.borrador()[rol] ?? []).filter(f => f.ver).length;
  }

  descartar(): void {
    const rol = this.rolActivo();
    this.borrador.update(b => ({ ...b, [rol]: structuredClone(this.original()[rol]) }));
  }

  guardar(): void {

    const rol = this.rolActivo();
    const filas = (this.borrador()[rol] ?? []).map(f => ({
      clave: f.clave, ver: f.ver, crear: f.crear, editar: f.editar, eliminar: f.eliminar
    }));

    this.guardando.set(true);

    this.permisos.guardarRol(rol, filas).subscribe({
      next: m => {
        this.guardando.set(false);
        // Conserva lo que se este editando en los otros roles.
        const otros = this.borrador();
        this.original.set(structuredClone(m.roles));
        this.borrador.set({ ...otros, [rol]: structuredClone(m.roles[rol]) });
        this.aviso.exito(`Permisos de ${this.nombreRol(rol)} guardados.`);

        // Si cambie los de mi propio rol, el menu tiene que reflejarlo ya.
        if (this.auth.rol() === rol) this.permisos.recargar().subscribe();
      },
      error: e => {
        this.guardando.set(false);
        this.aviso.errorHttp(e, 'No se pudieron guardar los permisos.');
      }
    });
  }

  nombreRol(rol: string): string {
    return this.roles.find(r => r.rol === rol)?.nombre ?? rol;
  }

  private aplicar(roles: Record<string, PermisoPantalla[]>): void {
    this.original.set(structuredClone(roles));
    this.borrador.set(structuredClone(roles));
  }
}
