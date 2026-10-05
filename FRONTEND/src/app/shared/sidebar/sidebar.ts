import { Component, HostListener, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { MenuLateralService } from '../../core/menu-lateral.service';
import { PermisosService } from '../../core/permisos.service';

/** Una entrada del menu. */
interface Enlace {
  ruta: string;
  texto: string;
  icono: string;
}

interface Seccion {
  titulo: string;
  enlaces: Enlace[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class SidebarComponent {

  private readonly auth = inject(AuthService);
  readonly menu = inject(MenuLateralService);

  readonly usuario = this.auth.usuario;
  readonly rol = this.auth.rol;

  private readonly permisos = inject(PermisosService);

  constructor() {
    // Por si se entra directo a una ruta sin guard de permisos (la ficha de una denuncia).
    this.permisos.asegurar().subscribe();
  }

  /**
   * Menu armado con las pantallas que el rol puede ver, agrupadas por seccion.
   * Antes era una lista fija con los roles escritos a mano en cada enlace.
   */
  readonly seccionesVisibles = computed<Seccion[]>(() => {

    const secciones = new Map<string, Enlace[]>();

    for (const p of this.permisos.pantallas()) {
      if (!p.ver || !p.enMenu) continue;
      const enlaces = secciones.get(p.seccion) ?? [];
      enlaces.push({ ruta: p.ruta, texto: p.nombre, icono: p.icono });
      secciones.set(p.seccion, enlaces);
    }

    return [...secciones].map(([titulo, enlaces]) => ({ titulo, enlaces }));
  });

  /** Escape cierra el cajon, como cualquier dialogo. */
  @HostListener('document:keydown.escape')
  alPulsarEscape(): void {
    this.menu.cerrar();
  }

  /**
   * Navegar cierra el cajon. En pantalla ancha el menu es fijo y cerrarlo no
   * tiene efecto visible, asi que no hace falta distinguir el caso.
   */
  alNavegar(): void {
    this.menu.cerrar();
  }

  iniciales(): string {
    const u = this.usuario();
    if (!u) return '';
    return `${u.nombre?.[0] ?? ''}${u.apellido?.[0] ?? ''}`.toUpperCase();
  }
}
