import { Component, HostListener, ElementRef, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { MenuLateralService } from '../../core/menu-lateral.service';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.html'
})
export class HeaderComponent {

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly host = inject(ElementRef<HTMLElement>);

  readonly menu = inject(MenuLateralService);

  readonly usuario = this.auth.usuario;
  readonly menuAbierto = signal(false);

  alternarMenu(): void {
    this.menuAbierto.update(v => !v);
  }

  /** Pulsar fuera cierra el desplegable del usuario. */
  @HostListener('document:click', ['$event'])
  alPulsarFuera(evento: MouseEvent): void {
    if (this.menuAbierto() && !this.host.nativeElement.contains(evento.target)) {
      this.menuAbierto.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  alPulsarEscape(): void {
    this.menuAbierto.set(false);
  }

  iniciales(): string {
    const u = this.usuario();
    if (!u) return '';
    return `${u.nombre?.[0] ?? ''}${u.apellido?.[0] ?? ''}`.toUpperCase();
  }

  cerrarSesion(): void {
    this.menuAbierto.set(false);
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
