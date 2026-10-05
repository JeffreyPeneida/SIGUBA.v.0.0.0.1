import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideToastr } from 'ngx-toastr';

import { App } from './app/app';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/auth.interceptor';

bootstrapApplication(App, {

  providers: [

    provideRouter(routes, withComponentInputBinding()),

    // El interceptor adjunta el JWT a cada peticion y cierra la sesion ante un 401.
    provideHttpClient(withInterceptors([authInterceptor])),

    provideAnimations(),

    provideToastr({
      positionClass: 'toast-bottom-right',
      timeOut: 4000,
      progressBar: true,
      closeButton: true,
      preventDuplicates: true,
      countDuplicates: true
    })

  ]

}).catch(err => console.error(err));
