import { Component, computed, effect, input, signal } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { Subscription } from 'rxjs';

/**
 * Envoltorio de un campo de formulario: etiqueta, icono, ayuda y error.
 *
 * El mismo bloque de 15 lineas estaba repetido en cada campo de cada
 * formulario, y el mensaje de error se escribia a mano en cada uno. Aqui el
 * mensaje se deduce del validador que falla, asi que decir "obligatorio" o
 * "minimo 6 caracteres" deja de depender de que alguien lo escriba bien.
 *
 * El input va por proyeccion de contenido: este componente no se mete con el
 * valor, solo con lo que lo rodea.
 */
@Component({
  selector: 'app-campo',
  standalone: true,
  templateUrl: './campo.html'
})
export class CampoComponent {

  readonly etiqueta = input.required<string>();
  readonly para = input<string>('');
  readonly icono = input<string>('');
  readonly ayuda = input<string>('');
  readonly opcional = input(false);

  /** El control cuyo estado decide si se muestra el error. */
  readonly control = input<AbstractControl | null>(null);

  /** Mensaje propio, cuando el generico no dice lo suficiente. */
  readonly error = input<string>('');

  /**
   * La aplicacion es zoneless y los formularios reactivos avisan por RxJS, no
   * por señales: sin esto los `computed()` de abajo calculaban una vez y se
   * quedaban ahi, y ningun mensaje de error llegaba a aparecer.
   *
   * `control.events` emite al cambiar el valor, el estado y al marcar el campo
   * como tocado, que es lo que decide si el error se muestra.
   */
  private readonly revision = signal(0);
  private suscripcion?: Subscription;

  constructor() {
    effect(onCleanup => {
      const c = this.control();
      this.suscripcion?.unsubscribe();

      if (c) {
        this.suscripcion = c.events.subscribe(() => this.revision.update(v => v + 1));
      }

      onCleanup(() => this.suscripcion?.unsubscribe());
    });
  }

  readonly enFallo = computed(() => {
    this.revision();
    const c = this.control();
    return !!c && c.invalid && (c.touched || c.dirty);
  });

  /** Traduce el validador que falla a algo que el usuario entienda. */
  readonly mensaje = computed<string>(() => {

    this.revision();

    if (this.error()) return this.error();

    const c = this.control();
    if (!c || !c.errors) return '';

    const e = c.errors;

    if (e['required'])  return `${this.etiqueta()} es obligatorio.`;
    if (e['email'])     return 'Escribe un correo válido, por ejemplo nombre@quito.gob.ec';
    if (e['minlength']) return `Debe tener al menos ${e['minlength'].requiredLength} caracteres.`;
    if (e['maxlength']) return `No puede superar los ${e['maxlength'].requiredLength} caracteres.`;
    if (e['pattern'])   return 'El formato no es válido.';
    if (e['cedula'])    return 'La cédula no es válida. Revisa los 10 dígitos.';
    if (e['min'])       return `El valor mínimo es ${e['min'].min}.`;
    if (e['max'])       return `El valor máximo es ${e['max'].max}.`;

    return 'Revisa este campo.';
  });

  /** Verde solo cuando el usuario ya escribio algo y es valido. */
  readonly correcto = computed(() => {
    this.revision();
    const c = this.control();
    return !!c && c.valid && (c.dirty || c.touched) && !!c.value;
  });
}
