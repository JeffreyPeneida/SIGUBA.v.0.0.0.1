import { Injectable, inject } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import Swal, { SweetAlertOptions } from 'sweetalert2';

/**
 * Unico sitio donde se avisa al usuario.
 *
 * Antes cada componente llamaba a `alert()` y `confirm()` del navegador: no se
 * podian estilar, bloqueaban la pagina entera y en automatizacion congelaban la
 * pestana. Aqui los avisos breves van como toast y las decisiones como dialogo.
 *
 * La regla: un toast informa de algo ya ocurrido; un dialogo pide una decision
 * antes de que ocurra.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionesService {

  private readonly toastr = inject(ToastrService);

  /** Colores y tipografia comunes a todos los dialogos. */
  private readonly baseDialogo: SweetAlertOptions = {
    buttonsStyling: false,
    reverseButtons: true,
    customClass: {
      popup: 'rounded-2xl',
      title: 'text-lg font-extrabold text-[var(--text)]',
      htmlContainer: 'text-sm text-[var(--muted)]',
      confirmButton:
        'h-11 min-w-[7rem] rounded-xl bg-[var(--primary)] px-5 text-sm font-bold text-white ' +
        'transition hover:bg-[var(--primary-dark)] mx-1',
      cancelButton:
        'h-11 min-w-[7rem] rounded-xl border border-[var(--border)] px-5 text-sm font-semibold ' +
        'text-[var(--text)] transition hover:border-[var(--primary)] mx-1',
      denyButton:
        'h-11 min-w-[7rem] rounded-xl bg-[var(--danger)] px-5 text-sm font-bold text-white ' +
        'transition hover:opacity-90 mx-1'
    }
  };

  // ------------------------------------------------------------------ toasts

  exito(mensaje: string, titulo = ''): void {
    this.toastr.success(mensaje, titulo);
  }

  error(mensaje: string, titulo = ''): void {
    this.toastr.error(mensaje, titulo);
  }

  aviso(mensaje: string, titulo = ''): void {
    this.toastr.warning(mensaje, titulo);
  }

  info(mensaje: string, titulo = ''): void {
    this.toastr.info(mensaje, titulo);
  }

  /**
   * Traduce un error HTTP a un mensaje que el usuario entienda.
   * El detalle tecnico se queda en la consola, no en pantalla.
   */
  errorHttp(e: unknown, porDefecto = 'No se pudo completar la operación.'): void {

    const estado = (e as { status?: number })?.status;
    const delServidor = (e as { error?: { mensaje?: string } })?.error?.mensaje;

    const mensaje =
      estado === 0   ? 'Sin conexión con el servidor.' :
      estado === 401 ? 'Tu sesión expiró. Vuelve a iniciar sesión.' :
      estado === 403 ? 'No tienes permisos para hacer esto.' :
      estado === 404 ? 'No se encontró lo que buscabas.' :
      estado === 409 ? (delServidor ?? 'Ese registro ya existe.') :
      (delServidor ?? porDefecto);

    console.error('[SIGUBA]', e);
    this.error(mensaje);
  }

  // ----------------------------------------------------------------- dialogos

  /** Confirmacion normal. Devuelve true si el usuario acepta. */
  async confirmar(
    titulo: string,
    texto: string,
    textoConfirmar = 'Confirmar'
  ): Promise<boolean> {

    const r = await Swal.fire({
      ...this.baseDialogo,
      icon: 'question',
      title: titulo,
      text: texto,
      showCancelButton: true,
      confirmButtonText: textoConfirmar,
      cancelButtonText: 'Cancelar'
    });

    return r.isConfirmed;
  }

  /**
   * Confirmacion de algo destructivo: boton rojo y foco en cancelar, para que
   * aceptar sea un acto deliberado y no el reflejo de pulsar Enter.
   */
  async confirmarPeligro(
    titulo: string,
    texto: string,
    textoConfirmar = 'Eliminar'
  ): Promise<boolean> {

    const r = await Swal.fire({
      ...this.baseDialogo,
      icon: 'warning',
      title: titulo,
      text: texto,
      showCancelButton: true,
      focusCancel: true,
      confirmButtonText: textoConfirmar,
      cancelButtonText: 'Cancelar',
      customClass: {
        ...(this.baseDialogo.customClass as object),
        confirmButton:
          'h-11 min-w-[7rem] rounded-xl bg-[var(--danger)] px-5 text-sm font-bold text-white ' +
          'transition hover:opacity-90 mx-1'
      }
    });

    return r.isConfirmed;
  }

  /** Mensaje de exito que requiere acuse, no un toast que se va solo. */
  async exitoDialogo(titulo: string, texto = ''): Promise<void> {
    await Swal.fire({
      ...this.baseDialogo,
      icon: 'success',
      title: titulo,
      text: texto,
      confirmButtonText: 'Entendido'
    });
  }

  /**
   * Ficha de ayuda de una pantalla.
   *
   * Admite HTML porque una explicacion util lleva lista de pasos; el contenido
   * lo escribe la propia pantalla, nunca viene del servidor ni del usuario.
   */
  async ayuda(titulo: string, html: string): Promise<void> {
    await Swal.fire({
      ...this.baseDialogo,
      icon: 'info',
      title: titulo,
      html,
      confirmButtonText: 'Entendido',
      width: '34rem'
    });
  }

  /** Bloquea la pantalla mientras dura una operacion larga. */
  cargando(titulo = 'Procesando…'): void {
    Swal.fire({
      ...this.baseDialogo,
      title: titulo,
      allowOutsideClick: false,
      allowEscapeKey: false,
      didOpen: () => Swal.showLoading()
    });
  }

  cerrarCargando(): void {
    Swal.close();
  }
}
