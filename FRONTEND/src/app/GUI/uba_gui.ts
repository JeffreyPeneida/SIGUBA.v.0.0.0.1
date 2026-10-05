import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { PermisosService } from '../core/permisos.service';
import { AuthService } from '../core/auth.service';
import { NotificacionesService } from '../core/notificaciones.service';
import { Tramite, TramitesApi } from '../core/tramites.api';

interface Acceso {
  ruta: string;
  titulo: string;
  descripcion: string;
  icono: string;
  color: string;
}

/**
 * Pantalla de inicio. Lo que muestra depende del rol: la administracion ve el
 * estado global, el tecnico ve su carga de trabajo y el usuario de consulta
 * solo lo que puede hacer.
 */
@Component({
  selector: 'app-gui',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './uba_gui.html'
})
export class UbaGuiComponent implements OnInit {

  private readonly auth = inject(AuthService);
  private readonly tramitesApi = inject(TramitesApi);
  private readonly aviso = inject(NotificacionesService);
  private readonly router = inject(Router);

  readonly usuario = this.auth.usuario;
  readonly rol = this.auth.rol;

  readonly tramites = signal<Tramite[]>([]);
  readonly cargando = signal(false);

  private readonly permisos = inject(PermisosService);

  readonly verOperacion = computed(() => this.permisos.puede('control-plagas'));

  readonly saludo = computed(() => {
    const h = new Date().getHours();
    return h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches';
  });

  readonly resumen = computed(() => {
    const t = this.tramites();
    return {
      total:          t.length,
      pendientes:     t.filter(x => !x.tecnico).length,
      asignados:      t.filter(x => x.tecnico && !x.fechaInspeccion).length,
      inspeccionados: t.filter(x => x.fechaInspeccion).length
    };
  });

  /** Accesos directos filtrados por lo que el rol puede hacer. */
  readonly accesos = computed<Acceso[]>(() => {

    const todos: (Acceso & { pantalla: string })[] = [
      {
        ruta: '/registrar-denuncia', titulo: 'Registrar denuncia',
        descripcion: 'Ingresa una nueva denuncia ciudadana',
        icono: 'fa-file-circle-plus', color: 'bg-[var(--primary-soft)] text-[var(--primary)]',
        pantalla: 'registrar-denuncia'
      },
      {
        ruta: '/usuario', titulo: 'Mis denuncias',
        descripcion: 'Seguimiento de lo que has denunciado',
        icono: 'fa-folder-open', color: 'bg-indigo-50 text-indigo-600',
        pantalla: 'mis-denuncias'
      },
      {
        ruta: '/control-plagas', titulo: 'Control de plagas',
        descripcion: 'Seguimiento de denuncias e inspecciones',
        icono: 'fa-bug', color: 'bg-amber-50 text-amber-600',
        pantalla: 'control-plagas'
      },
      {
        ruta: '/tecnico', titulo: 'Mis trámites',
        descripcion: 'Trámites asignados para inspección',
        icono: 'fa-helmet-safety', color: 'bg-sky-50 text-sky-600',
        pantalla: 'inspecciones'
      },
      {
        ruta: '/gestionar-usuarios', titulo: 'Usuarios',
        descripcion: 'Altas, edición y bajas de cuentas',
        icono: 'fa-users', color: 'bg-violet-50 text-violet-600',
        pantalla: 'usuarios'
      },
      {
        ruta: '/gestionar-tecnicos', titulo: 'Técnicos',
        descripcion: 'Personal de campo y asignaciones',
        icono: 'fa-user-gear', color: 'bg-emerald-50 text-emerald-600',
        pantalla: 'tecnicos'
      },
      {
        ruta: '/catalogos', titulo: 'Catálogos',
        descripcion: 'Listas de los formularios',
        icono: 'fa-layer-group', color: 'bg-orange-50 text-orange-600',
        pantalla: 'catalogos'
      },
      {
        ruta: '/perfiles', titulo: 'Perfiles y permisos',
        descripcion: 'Qué ve y qué hace cada rol',
        icono: 'fa-user-shield', color: 'bg-rose-50 text-rose-600',
        pantalla: 'perfiles'
      }
    ];

    // Solo lo que el rol tiene permitido en Perfiles.
    return todos.filter(a => this.permisos.puede(a.pantalla));
  });

  ngOnInit(): void {

    // El estado global de la operacion solo lo ve quien puede entrar a Control de plagas.
    this.permisos.asegurar().subscribe(() => {

      if (!this.verOperacion()) return;

      this.cargando.set(true);

      this.tramitesApi.todos().subscribe({
        next: l => { this.tramites.set(l ?? []); this.cargando.set(false); },
        error: e => { this.cargando.set(false); this.aviso.errorHttp(e); }
      });
    });
  }

  ir(ruta: string): void {
    this.router.navigateByUrl(ruta);
  }
}
