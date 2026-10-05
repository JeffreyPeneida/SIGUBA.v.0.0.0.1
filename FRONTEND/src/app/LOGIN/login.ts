import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
    FormBuilder,
    FormGroup,
    Validators,
    ReactiveFormsModule
} from '@angular/forms';

import { AuthService } from '../core/auth.service';
import { PermisosService } from '../core/permisos.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './login.html'
})
export class LoginComponent implements OnInit {

    private readonly fb = inject(FormBuilder);
    private readonly auth = inject(AuthService);
    private readonly permisos = inject(PermisosService);
    private readonly router = inject(Router);

    formLogin!: FormGroup;

    readonly anio = new Date().getFullYear();
    readonly mostrarPassword = signal(false);
    readonly cargando = signal(false);
    readonly error = signal<string | null>(null);

    /** Avanza con cada cambio del formulario; ver invalido(). */
    private readonly revision = signal(0);

    ngOnInit(): void {

        this.formLogin = this.fb.group({
            usuario: ['', [Validators.required, Validators.minLength(3)]],
            password: ['', [Validators.required, Validators.minLength(6)]]
        });

        this.formLogin.events.subscribe(() => this.revision.update(v => v + 1));
    }

    /**
     * True si el campo esta mal y el usuario ya lo toco.
     *
     * Se apoya en una señal que avanza con cada evento del formulario: en una
     * aplicacion zoneless, leer el estado del control directamente desde la
     * plantilla no basta para que se vuelva a pintar.
     */
    invalido(campo: string): boolean {
        this.revision();
        const c = this.formLogin?.get(campo);
        return !!c && c.invalid && (c.touched || c.dirty);
    }

    alternarPassword(): void {
        this.mostrarPassword.update(v => !v);
    }

    iniciarSesion(): void {

        this.error.set(null);

        if (this.formLogin.invalid) {
            this.formLogin.markAllAsTouched();
            return;
        }

        const { usuario, password } = this.formLogin.value;

        this.cargando.set(true);

        this.auth.login(usuario, password).subscribe({

            next: () => {
                // Cada usuario entra a su pantalla inicial, que depende de
                // los permisos de su rol: hay que tenerlos antes de navegar.
                this.permisos.asegurar().subscribe(() => {
                    this.cargando.set(false);
                    this.router.navigateByUrl(this.permisos.rutaInicial());
                });
            },

            error: (e: unknown) => {
                this.cargando.set(false);

                const estado = (e as { status?: number })?.status;

                this.error.set(
                    estado === 401
                        ? 'Usuario o contraseña incorrectos.'
                        : 'No se pudo conectar con el servidor. Intenta de nuevo.'
                );
            }
        });
    }

    registrarUsuario(): void {
        this.router.navigate(['/registro']);
    }
}
