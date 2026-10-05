import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { PermisosService } from '../core/permisos.service';
import { AuthService } from '../core/auth.service';
import { NotificacionesService } from '../core/notificaciones.service';
import { Tramite, TramitesApi } from '../core/tramites.api';
import { TramitesTablaComponent } from '../shared/tramites-tabla/tramites-tabla';
import { AsignarTecnicoComponent } from '../shared/asignar-tecnico/asignar-tecnico';

type Vista = 'todos' | 'pendientes' | 'asignados' | 'inspeccionados';

@Component({
    selector: 'app-control-plagas',
    standalone: true,
    imports: [CommonModule, TramitesTablaComponent, AsignarTecnicoComponent],
    templateUrl: './control_plagas.html'
})
export class ControlPlagasComponent implements OnInit {

    private readonly tramitesApi = inject(TramitesApi);
    private readonly auth = inject(AuthService);
    private readonly aviso = inject(NotificacionesService);
    private readonly router = inject(Router);

    readonly tramites = signal<Tramite[]>([]);
    readonly cargando = signal(false);
    readonly vista = signal<Vista>('todos');
    readonly filtro = signal('');

    /** Tramite abierto en el dialogo de asignacion; null lo cierra. */
    readonly asignando = signal<Tramite | null>(null);

    private readonly permisos = inject(PermisosService);

    /** Asignar técnico es editar el trámite; anular y borrar, eliminarlo. Se configura en Perfiles. */
    readonly puedeAsignar = computed(() => this.permisos.puede('control-plagas', 'EDITAR'));
    readonly puedeBorrar = computed(() => this.permisos.puede('control-plagas', 'ELIMINAR'));
    readonly puedeInspeccionar = computed(() => this.permisos.puede('inspecciones', 'CREAR'));
    readonly puedeInforme = computed(() => this.permisos.puede('inspecciones'));

    readonly vistas: { clave: Vista; texto: string; icono: string }[] = [
        { clave: 'todos',          texto: 'Todos',          icono: 'fa-list' },
        { clave: 'pendientes',     texto: 'Sin técnico',    icono: 'fa-clock' },
        { clave: 'asignados',      texto: 'Asignados',      icono: 'fa-user-check' },
        { clave: 'inspeccionados', texto: 'Inspeccionados', icono: 'fa-clipboard-check' }
    ];

    readonly visibles = computed<Tramite[]>(() => {

        const texto = this.filtro().trim().toLowerCase();

        return this.tramites().filter(t => {

            switch (this.vista()) {
                case 'pendientes':     if (t.tecnico) return false; break;
                case 'asignados':      if (!t.tecnico || t.fechaInspeccion) return false; break;
                case 'inspeccionados': if (!t.fechaInspeccion) return false; break;
            }

            if (!texto) return true;

            const denunciante = `${t.denunciante?.nombre ?? ''} ${t.denunciante?.apellido ?? ''}`;

            return [t.idTramite, denunciante, t.ubicacion?.direccion, t.barrio?.nombre, t.narracion]
                .some(c => (c ?? '').toLowerCase().includes(texto));
        });
    });

    /** Contadores del resumen superior. */
    readonly resumen = computed(() => {
        const t = this.tramites();
        return {
            total:          t.length,
            pendientes:     t.filter(x => !x.tecnico).length,
            asignados:      t.filter(x => x.tecnico && !x.fechaInspeccion).length,
            inspeccionados: t.filter(x => x.fechaInspeccion).length
        };
    });

    ngOnInit(): void {
        this.cargar();
    }

    cargar(): void {

        this.cargando.set(true);

        this.tramitesApi.todos().subscribe({
            next: lista => {
                this.tramites.set(lista ?? []);
                this.cargando.set(false);
            },
            error: e => {
                this.cargando.set(false);
                this.aviso.errorHttp(e, 'No se pudieron cargar los trámites.');
            }
        });
    }

    /** Ver es consultar: lleva a la ficha, no al formulario de inspeccion. */
    verDetalle(t: Tramite): void {
        this.router.navigate(['/denuncia', t.idCodigoInterno]);
    }

    abrirInforme(t: Tramite): void {
        this.router.navigate(['/informe-inspeccion', t.idCodigoInterno]);
    }

    inspeccionar(t: Tramite): void {
        this.router.navigate(['/reginspeccion'], {
            queryParams: { tramite: t.idCodigoInterno }
        });
    }

    /**
     * Antes esto navegaba a la pantalla de tecnicos con el identificador en la
     * URL, y esa pantalla lo ignoraba: no asignaba nada. Ahora se resuelve aqui.
     */
    asignar(t: Tramite): void {
        this.asignando.set(t);
    }

    /** Tras asignar hay que releer: cambia el tecnico y la etapa del tramite. */
    trasAsignar(): void {
        this.cargar();
    }

    /** Anular deja el tramite en la base como INACTIVO: es reversible. */
    async anular(t: Tramite): Promise<void> {

        const ok = await this.aviso.confirmar(
            'Anular el trámite',
            `${t.idTramite ?? 'El trámite'} dejará de aparecer en los listados, `
            + 'pero se conserva y se puede restaurar.',
            'Anular');

        if (!ok) return;

        this.tramitesApi.eliminar(t.idCodigoInterno).subscribe({
            next: () => {
                this.aviso.exito(`${t.idTramite ?? 'El trámite'} quedó anulado.`);
                this.cargar();
            },
            error: e => this.aviso.errorHttp(e, 'No se pudo anular el trámite.')
        });
    }

    /**
     * Borrado definitivo. Se pide confirmacion en dos pasos porque no hay vuelta
     * atras: tambien desaparecen las fotos.
     */
    async borrar(t: Tramite): Promise<void> {

        const codigo = t.idTramite ?? `#${t.idCodigoInterno}`;

        const ok = await this.aviso.confirmarPeligro(
            'Borrar definitivamente',
            `${codigo} y sus fotos se borrarán para siempre. Esta acción no se `
            + 'puede deshacer. Si solo quieres ocultarlo, usa «Anular».',
            'Borrar para siempre');

        if (!ok) return;

        this.tramitesApi.borrarDefinitivo(t.idCodigoInterno).subscribe({
            next: () => {
                this.aviso.exito(`${codigo} se borró definitivamente.`);
                this.cargar();
            },
            error: e => this.aviso.errorHttp(e, 'No se pudo borrar el trámite.')
        });
    }
}
