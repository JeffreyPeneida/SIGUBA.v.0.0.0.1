import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';

import { UsuariosApi } from '../core/usuarios.api';
import { CampoComponent } from '../shared/campo/campo';
import { SelectBuscadorComponent } from '../shared/select-buscador/select-buscador';
import { AdminZonal, AdminZonalApi } from '../core/adminzonal.api';

/** Las dos contrasenas deben coincidir. */
function passwordsIguales(grupo: AbstractControl): ValidationErrors | null {
  const a = grupo.get('password')?.value;
  const b = grupo.get('confirmar')?.value;
  return a && b && a !== b ? { noCoinciden: true } : null;
}

/**
 * Cedula ecuatoriana: 10 digitos, provincia valida y digito verificador.
 * Antes solo se comprobaba la longitud y entraba cualquier numero.
 */
function cedulaEcuatoriana(control: AbstractControl): ValidationErrors | null {

  const valor: string = control.value ?? '';

  if (!valor) return null;
  if (!/^\d{10}$/.test(valor)) return { cedula: true };

  const provincia = Number(valor.slice(0, 2));
  if (provincia < 1 || provincia > 24) return { cedula: true };

  const digitos = valor.split('').map(Number);
  let suma = 0;

  for (let i = 0; i < 9; i++) {
    let n = digitos[i];
    if (i % 2 === 0) {
      n *= 2;
      if (n > 9) n -= 9;
    }
    suma += n;
  }

  const verificador = (10 - (suma % 10)) % 10;

  return verificador === digitos[9] ? null : { cedula: true };
}

@Component({
  selector: 'app-registro-usuario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CampoComponent, SelectBuscadorComponent],
  templateUrl: './regis_user.html'
})
export class RegistroUsuarioComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly usuariosApi = inject(UsuariosApi);
  private readonly adminZonalApi = inject(AdminZonalApi);
  private readonly router = inject(Router);

  formulario!: FormGroup;

  readonly adminZonales = signal<AdminZonal[]>([]);
  readonly mostrarPassword = signal(false);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly exito = signal(false);

  /** Nadie menor de edad ni con fecha futura. */
  readonly fechaMaxima = (() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.toISOString().slice(0, 10);
  })();

  ngOnInit(): void {

    this.formulario = this.fb.group({
      nombre:          ['', [Validators.required, Validators.maxLength(150)]],
      apellido:        ['', [Validators.required, Validators.maxLength(150)]],
      cedula:          ['', [Validators.required, cedulaEcuatoriana]],
      fechaNacimiento: ['', [Validators.required]],
      usuario:         ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      mail:            ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      adminZonal:      [''],
      password:        ['', [Validators.required, Validators.minLength(6)]],
      confirmar:       ['', [Validators.required]]
    }, { validators: passwordsIguales });

    // Mantiene la señal al dia con lo que el usuario escribe.
    this.formulario.get('password')!.valueChanges.subscribe(
      (v: string) => this.passwordEscrito.set(v ?? ''));

    this.adminZonalApi.listar().subscribe({
      next: lista => this.adminZonales.set(lista),
      error: () => this.adminZonales.set([])   // el desplegable queda vacio, no rompe el alta
    });
  }

  /**
   * Fuerza de la contrasena, de 0 a 4. No bloquea el registro: solo orienta,
   * porque una regla rigida empuja a la gente a patrones predecibles.
   */
  /**
   * El valor se lee de una señal, no del control: en una aplicacion zoneless un
   * computed que lea `control.value` directamente no vuelve a calcularse nunca.
   */
  private readonly passwordEscrito = signal('');

  readonly fuerza = computed(() => {

    const v: string = this.passwordEscrito();

    if (!v) return { nivel: 0, texto: '', color: '' };

    let n = 0;
    if (v.length >= 8) n++;
    if (/[a-z]/.test(v) && /[A-Z]/.test(v)) n++;
    if (/\d/.test(v)) n++;
    if (/[^A-Za-z0-9]/.test(v)) n++;

    const escala = [
      { nivel: 1, texto: 'Débil',      color: 'bg-red-500' },
      { nivel: 2, texto: 'Aceptable',  color: 'bg-amber-500' },
      { nivel: 3, texto: 'Buena',      color: 'bg-sky-500' },
      { nivel: 4, texto: 'Muy buena',  color: 'bg-emerald-500' }
    ];

    return escala[Math.max(0, n - 1)];
  });

  campo(nombre: string) {
    return this.formulario?.get(nombre) ?? null;
  }

  invalido(campo: string): boolean {
    const c = this.formulario?.get(campo);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  get noCoinciden(): boolean {
    return this.formulario?.hasError('noCoinciden')
        && !!this.formulario.get('confirmar')?.touched;
  }

  alternarPassword(): void {
    this.mostrarPassword.update(v => !v);
  }

  registrar(): void {

    this.error.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const v = this.formulario.value;

    this.cargando.set(true);

    // El rol no se envia: el backend asigna USUARIO a todo registro publico.
    this.usuariosApi.crear({
      nombre: v.nombre.trim(),
      apellido: v.apellido.trim(),
      cedula: v.cedula.trim(),
      fechaNacimiento: v.fechaNacimiento,
      usuario: v.usuario.trim(),
      mail: v.mail.trim(),
      password: v.password,
      rol: 'USUARIO',
      adminZonal: v.adminZonal ? { idAdminZonal: Number(v.adminZonal) } : null
    }).subscribe({

      next: () => {
        this.cargando.set(false);
        this.exito.set(true);
        setTimeout(() => this.router.navigate(['/login']), 2200);
      },

      error: (e: unknown) => {
        this.cargando.set(false);

        const estado = (e as { status?: number })?.status;
        const mensaje = (e as { error?: { mensaje?: string } })?.error?.mensaje;

        this.error.set(
          estado === 409
            ? 'Ya existe un usuario con esa cédula o ese nombre de usuario.'
            : mensaje ?? 'No se pudo completar el registro. Intenta de nuevo.'
        );
      }
    });
  }

  volver(): void {
    this.router.navigate(['/login']);
  }
}
