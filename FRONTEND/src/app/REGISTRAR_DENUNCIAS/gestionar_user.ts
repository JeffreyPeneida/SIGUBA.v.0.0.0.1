import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Rol } from '../core/auth.models';
import { Usuario, UsuariosApi } from '../core/usuarios.api';
import { AdminZonal, AdminZonalApi } from '../core/adminzonal.api';
import { CampoComponent } from '../shared/campo/campo';
import { PermisosService } from '../core/permisos.service';
import { AuthService } from '../core/auth.service';
import { NotificacionesService } from '../core/notificaciones.service';
import { SelectBuscadorComponent } from '../shared/select-buscador/select-buscador';

type Modo = 'crear' | 'editar';

@Component({
  selector: 'app-gestionar-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CampoComponent, SelectBuscadorComponent],
  templateUrl: './gestionar_user.html'
})
export class GestionarUserComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly usuariosApi = inject(UsuariosApi);
  private readonly adminZonalApi = inject(AdminZonalApi);
  private readonly permisos = inject(PermisosService);
  private readonly auth = inject(AuthService);
  private readonly notificaciones = inject(NotificacionesService);

  readonly puedeCrear = computed(() => this.permisos.puede('usuarios', 'CREAR'));
  readonly puedeEditar = computed(() => this.permisos.puede('usuarios', 'EDITAR'));
  readonly puedeDesactivar = computed(() => this.permisos.puede('usuarios', 'ELIMINAR'));

  /** 'ACTIVO' por defecto: lo habitual es trabajar con las cuentas en uso. */
  readonly estadoFiltro = signal<'ACTIVO' | 'INACTIVO' | ''>('ACTIVO');

  readonly totales = computed(() => {
    const u = this.usuarios();
    const inactivos = u.filter(x => x.estado === 'INACTIVO').length;
    return { activos: u.length - inactivos, inactivos };
  });

  readonly usuarios = signal<Usuario[]>([]);
  readonly adminZonales = signal<AdminZonal[]>([]);

  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);

  /** Avanza con cada evento del formulario; ver invalido(). */
  private readonly revision = signal(0);

  readonly filtro = signal('');
  readonly rolFiltro = signal<Rol | ''>('');

  readonly panelAbierto = signal(false);
  readonly modo = signal<Modo>('crear');
  readonly editando = signal<Usuario | null>(null);

  readonly roles: Rol[] = ['ADMIN', 'TECNICO', 'USUARIO'];

  formulario!: FormGroup;

  /** Filtrado en memoria: la lista completa cabe de sobra. */
  readonly visibles = computed<Usuario[]>(() => {

    const texto = this.filtro().trim().toLowerCase();
    const rol = this.rolFiltro();
    const estado = this.estadoFiltro();

    return this.usuarios().filter(u => {

      if (rol && u.rol !== rol) return false;
      if (estado && (u.estado ?? 'ACTIVO') !== estado) return false;
      if (!texto) return true;

      return [u.nombre, u.apellido, u.cedula, u.usuario, u.mail]
        .some(c => (c ?? '').toLowerCase().includes(texto));
    });
  });

  ngOnInit(): void {

    this.formulario = this.fb.group({
      nombre:          ['', [Validators.required]],
      apellido:        ['', [Validators.required]],
      cedula:          ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      fechaNacimiento: ['', [Validators.required]],
      usuario:         ['', [Validators.required, Validators.minLength(3)]],
      mail:            ['', [Validators.required, Validators.email]],
      rol:             ['USUARIO' as Rol, [Validators.required]],
      adminZonal:      [''],
      password:        ['']
    });

    this.formulario.events.subscribe(() => this.revision.update(v => v + 1));

    this.cargar();

    this.adminZonalApi.listar().subscribe({
      next: l => this.adminZonales.set(l),
      error: () => this.adminZonales.set([])
    });
  }

  cargar(): void {

    this.cargando.set(true);
    this.error.set(null);

    this.usuariosApi.listar(true).subscribe({
      next: lista => {
        this.usuarios.set(lista ?? []);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de usuarios.');
        this.cargando.set(false);
      }
    });
  }

  // ---------------------------------------------------------------- panel

  abrirCrear(): void {
    this.modo.set('crear');
    this.editando.set(null);
    this.formulario.reset({ rol: 'USUARIO', adminZonal: '' });
    this.formulario.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.formulario.get('password')?.updateValueAndValidity();
    this.panelAbierto.set(true);
  }

  abrirEditar(u: Usuario): void {
    this.modo.set('editar');
    this.editando.set(u);

    this.formulario.patchValue({
      nombre: u.nombre,
      apellido: u.apellido,
      cedula: u.cedula,
      fechaNacimiento: (u.fechaNacimiento ?? '').slice(0, 10),
      usuario: u.usuario,
      mail: u.mail ?? '',
      rol: u.rol,
      adminZonal: u.adminZonal?.idAdminZonal ?? '',
      password: ''
    });

    // Al editar, la contrasena solo cambia si se escribe una nueva.
    this.formulario.get('password')?.setValidators([Validators.minLength(6)]);
    this.formulario.get('password')?.updateValueAndValidity();
    this.panelAbierto.set(true);
  }

  cerrarPanel(): void {
    this.panelAbierto.set(false);
    this.error.set(null);
  }

  campo(nombre: string) {
    return this.formulario?.get(nombre) ?? null;
  }

  /** Zoneless: sin leer la señal, el estado del control no repinta. */
  invalido(campo: string): boolean {
    this.revision();
    const c = this.formulario?.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  guardar(): void {

    this.error.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const v = this.formulario.value;

    const datos: Usuario = {
      nombre: v.nombre.trim(),
      apellido: v.apellido.trim(),
      cedula: v.cedula.trim(),
      fechaNacimiento: v.fechaNacimiento,
      usuario: v.usuario.trim(),
      mail: v.mail.trim(),
      rol: v.rol,
      adminZonal: v.adminZonal ? { idAdminZonal: Number(v.adminZonal) } : null
    };

    if (v.password) {
      datos.password = v.password;
    }

    this.guardando.set(true);

    const peticion = this.modo() === 'crear'
      ? this.usuariosApi.crear(datos)
      : this.usuariosApi.actualizar(this.editando()!.idUsuario!, datos);

    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.panelAbierto.set(false);
        this.anunciar(this.modo() === 'crear'
          ? 'Usuario creado correctamente.'
          : 'Usuario actualizado correctamente.');
        this.cargar();
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        const estado = (e as { status?: number })?.status;
        this.error.set(
          estado === 409
            ? 'Ya existe un usuario con esa cédula o ese nombre de usuario.'
            : 'No se pudo guardar. Revisa los datos e intenta de nuevo.'
        );
      }
    });
  }

  // ---------------------------------------------------------------- bajas

  /** La propia cuenta no se puede desactivar: cerraria la sesion que puede deshacerlo. */
  esYo(u: Usuario): boolean {
    return u.cedula === this.auth.usuario()?.cedula;
  }

  async desactivar(u: Usuario): Promise<void> {

    const ok = await this.notificaciones.confirmarPeligro(
      'Desactivar usuario',
      `${u.nombre} ${u.apellido} no podrá iniciar sesión y se cerrará su sesión abierta. `
      + 'No se borra nada: puedes activarlo de nuevo cuando quieras.',
      'Desactivar');

    if (!ok) return;

    // Baja logica: el backend marca ESTADO = 'INACTIVO', no borra la fila.
    this.usuariosApi.eliminar(u.cedula).subscribe({
      next: () => {
        this.anunciar(`${u.nombre} ${u.apellido} quedó desactivado.`);
        this.cargar();
      },
      error: e => this.error.set(e?.error?.mensaje ?? 'No se pudo desactivar al usuario.')
    });
  }

  activar(u: Usuario): void {
    this.usuariosApi.restaurar(u.cedula).subscribe({
      next: () => {
        this.anunciar(`${u.nombre} ${u.apellido} vuelve a estar activo.`);
        this.cargar();
      },
      error: e => this.error.set(e?.error?.mensaje ?? 'No se pudo activar al usuario.')
    });
  }

  private anunciar(mensaje: string): void {
    this.aviso.set(mensaje);
    setTimeout(() => this.aviso.set(null), 4000);
  }

  /** Un icono por rol: se distinguen de un vistazo sin leer la etiqueta. */
  iconoRol(rol: Rol): string {
    switch (rol) {
      case 'ADMIN':   return 'fa-user-shield';
      case 'TECNICO': return 'fa-helmet-safety';
      default:        return 'fa-user';
    }
  }

  colorRol(rol: Rol): string {
    switch (rol) {
      case 'ADMIN':   return 'bg-violet-50 text-violet-700 ring-violet-200';
      case 'TECNICO': return 'bg-sky-50 text-sky-700 ring-sky-200';
      default:        return 'bg-slate-100 text-slate-600 ring-slate-200';
    }
  }

  iniciales(u: Usuario): string {
    return `${u.nombre?.[0] ?? ''}${u.apellido?.[0] ?? ''}`.toUpperCase();
  }
}
