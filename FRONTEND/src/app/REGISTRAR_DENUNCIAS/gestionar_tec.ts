import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { PermisosService } from '../core/permisos.service';
import { CommonModule } from '@angular/common';

import { Rol } from '../core/auth.models';
import { NotificacionesService } from '../core/notificaciones.service';
import { Usuario, UsuariosApi } from '../core/usuarios.api';

/**
 * Personal de campo.
 *
 * Los tecnicos son usuarios con rol TECNICO: la autenticacion se unifico en
 * UBA_USUARIO y la tabla UBA_TECNICO quedo solo como el registro profesional al
 * que apuntan los tramites. Por eso esta pantalla trabaja sobre /api/usuarios
 * filtrando por rol, y no sobre una API de tecnicos aparte.
 */
@Component({
    selector: 'app-gestionar-tecnicos',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './gestionar_tec.html'
})
export class GestionarTecComponent implements OnInit {

    private readonly usuariosApi = inject(UsuariosApi);
    private readonly aviso = inject(NotificacionesService);

    private readonly permisos = inject(PermisosService);
    readonly puedeDarDeBaja = computed(() => this.permisos.puede('tecnicos', 'ELIMINAR'));

    readonly tecnicos = signal<Usuario[]>([]);
    readonly cargando = signal(false);
    readonly filtro = signal('');

    readonly visibles = computed<Usuario[]>(() => {

        const texto = this.filtro().trim().toLowerCase();
        if (!texto) return this.tecnicos();

        return this.tecnicos().filter(t =>
            [t.nombre, t.apellido, t.cedula, t.usuario, t.mail]
                .some(c => (c ?? '').toLowerCase().includes(texto)));
    });

    ngOnInit(): void {
        this.cargar();
    }

    cargar(): void {

        this.cargando.set(true);

        this.usuariosApi.porRol('TECNICO' as Rol).subscribe({
            next: lista => {
                this.tecnicos.set(lista ?? []);
                this.cargando.set(false);
            },
            error: e => {
                this.cargando.set(false);
                this.aviso.errorHttp(e, 'No se pudo cargar el personal técnico.');
            }
        });
    }

    /** Quitar el rol TECNICO es una baja: se confirma con dialogo, no con toast. */
    async darDeBaja(t: Usuario): Promise<void> {

        const ok = await this.aviso.confirmarPeligro(
            'Dar de baja al técnico',
            `${t.nombre} ${t.apellido} dejará de estar disponible para nuevas `
            + 'asignaciones. Los trámites que ya tiene asignados no cambian.',
            'Dar de baja');

        if (!ok) return;

        this.usuariosApi.eliminar(t.cedula).subscribe({
            next: () => {
                this.aviso.exito(`${t.nombre} ${t.apellido} quedó inactivo.`);
                this.cargar();
            },
            error: e => this.aviso.errorHttp(e, 'No se pudo dar de baja al técnico.')
        });
    }

    iniciales(t: Usuario): string {
        return `${t.nombre?.[0] ?? ''}${t.apellido?.[0] ?? ''}`.toUpperCase();
    }
}
