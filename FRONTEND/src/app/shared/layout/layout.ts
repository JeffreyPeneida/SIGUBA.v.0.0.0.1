import { Component, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { MenuLateralService } from '../../core/menu-lateral.service';
import { SidebarComponent } from '../sidebar/sidebar';
import { HeaderComponent } from '../header/header';

/**
 * Armazon de las pantallas con sesion iniciada: menu lateral, cabecera pegajosa
 * y el contenido de la ruta. El login queda fuera a proposito.
 *
 * Por encima de 1280px el menu es fijo y el contenido se desplaza para dejarle
 * sitio; por debajo, el menu es un cajon y el contenido ocupa todo el ancho.
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="min-h-screen bg-[var(--background)]">
      <app-sidebar />

      <div class="flex min-h-screen flex-col p-3 sm:p-4 xl:ml-[var(--sidebar-width)]">
        <div class="sticky top-3 z-30 sm:top-4">
          <app-header />
        </div>

        <main class="min-w-0 flex-1 pt-4 sm:pt-5">
          <router-outlet />
        </main>

        <footer class="pt-6 text-center text-xs leading-5 text-[var(--muted)]">
          Municipio del Distrito Metropolitano de Quito
          <span class="hidden sm:inline">·</span>
          <span class="block sm:inline">Unidad de Bienestar Animal</span>
        </footer>
      </div>
    </div>
  `
})
export class LayoutComponent {

  private readonly menu = inject(MenuLateralService);

  constructor(router: Router) {
    // Cambiar de pantalla cierra el cajon: dejarlo abierto tapando el
    // contenido recien cargado desorienta.
    router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => this.menu.cerrar());
  }
}
