import { Signal, computed, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl } from '@angular/forms';
import { startWith } from 'rxjs';

/**
 * Convierte un control de formulario en una señal.
 *
 * La aplicación es zoneless: la deteccion de cambios se dispara con señales y
 * con eventos ligados en plantilla. Los formularios reactivos, en cambio,
 * avisan por RxJS, asi que un `computed()` que lea `control.invalid` calcula
 * una vez y se queda con ese valor para siempre: los mensajes de error no
 * aparecian nunca.
 *
 * `control.events` emite en cada cambio de valor, de estado y al marcar el
 * control como tocado, que es justo lo que decide si se muestra un error.
 */
export function señalDeControl(control: AbstractControl | null): Signal<AbstractControl | null> {

  if (!control) {
    return signal(null);
  }

  // El valor emitido no importa; lo que importa es que la señal cambie para
  // que quien la lea vuelva a calcular contra el estado actual del control.
  const eventos = toSignal(control.events.pipe(startWith(null)), { initialValue: null });

  return computed(() => {
    eventos();
    return control;
  });
}
