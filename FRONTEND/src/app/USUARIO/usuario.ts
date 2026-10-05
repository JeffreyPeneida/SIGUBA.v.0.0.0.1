import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { NotificacionesService } from '../core/notificaciones.service';
import { Tramite, TramitesApi } from '../core/tramites.api';
import { TramitesTablaComponent } from '../shared/tramites-tabla/tramites-tabla';

/**
 * Vista del usuario de consulta: sus denuncias y en que estado van.
 *
 * No muestra el tecnico asignado ni acciones de gestion; para eso estan las
 * pantallas de administracion.
 */
@Component({
    selector: 'app-usuario',
    standalone: true,
    imports: [CommonModule, TramitesTablaComponent],
    templateUrl: './usuario.html'
})
export class UsuarioComponent implements OnInit {

    private readonly auth = inject(AuthService);
    private readonly tramitesApi = inject(TramitesApi);
    private readonly aviso = inject(NotificacionesService);
    private readonly router = inject(Router);

    readonly usuario = this.auth.usuario;

    readonly tramites = signal<Tramite[]>([]);
    readonly cargando = signal(false);
    readonly filtro = signal('');

    readonly visibles = computed<Tramite[]>(() => {

        const texto = this.filtro().trim().toLowerCase();
        if (!texto) return this.tramites();

        return this.tramites().filter(t =>
            [t.idTramite, t.ubicacion?.direccion, t.barrio?.nombre, t.narracion]
                .some(c => (c ?? '').toLowerCase().includes(texto)));
    });

    readonly resumen = computed(() => {
        const t = this.tramites();
        return {
            total:       t.length,
            enProceso:   t.filter(x => !x.fechaInspeccion).length,
            atendidos:   t.filter(x => x.fechaInspeccion).length
        };
    });

    ngOnInit(): void {
        this.cargar();
    }

    cargar(): void {

        this.cargando.set(true);

        // El backend resuelve de quien son a partir de la sesion. Antes se
        // pedia /buscarTramites y cada usuario veia las denuncias de todos.
        this.tramitesApi.misDenuncias().subscribe({
            next: lista => {
                this.tramites.set(lista ?? []);
                this.cargando.set(false);
            },
            error: e => {
                this.cargando.set(false);
                this.aviso.errorHttp(e, 'No se pudieron cargar tus denuncias.');
            }
        });
    }

    /** El botón existía en la tabla pero nadie escuchaba su salida. */
    verDetalle(t: Tramite): void {
        this.router.navigate(['/denuncia', t.idCodigoInterno]);
    }

    nuevaDenuncia(): void {
        this.router.navigate(['/registrar-denuncia']);
    }
}
