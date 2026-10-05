import { Routes } from '@angular/router';

import { authGuard, invitadoGuard, permisoGuard } from './core/auth.guard';
import { LayoutComponent } from './shared/layout/layout';

import { LoginComponent } from './LOGIN/login';
import { RegistroUsuarioComponent } from './REGISTRAR_USUARIOS/regis_user';
import { UbaGuiComponent } from './GUI/uba_gui';
import { RegistrarDenunciaComponent } from './REGISTRAR_DENUNCIAS/regis_denuncia';
import { ControlPlagasComponent } from './REGISTRAR_DENUNCIAS/control_plagas';
import { GestionarUserComponent } from './REGISTRAR_DENUNCIAS/gestionar_user';
import { GestionarTecComponent } from './REGISTRAR_DENUNCIAS/gestionar_tec';
import { UsuarioComponent } from './USUARIO/usuario';
import { RegInspeccionComponent } from './TECNICO/reginspeccion';
import { TecnicoComponent } from './TECNICO/tecnico';
import { CatalogosComponent } from './CATALOGOS/catalogos';
import { DetalleDenunciaComponent } from './DETALLE/detalle-denuncia';
import { PerfilesComponent } from './PERFILES/perfiles';
import { InformeInspeccionComponent } from './INFORME/informe-inspeccion';

// La aplicacion abre en el login, que va sin armazon. Todo lo demas cuelga de
// LayoutComponent (menu lateral + cabecera) y exige sesion iniciada.
export const routes: Routes = [

  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // --- publicas, sin armazon -------------------------------------------
  { path: 'login',    component: LoginComponent,          canActivate: [invitadoGuard] },
  { path: 'registro', component: RegistroUsuarioComponent },

  // --- con sesion, dentro del armazon -----------------------------------
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      // Quien entra a cada pantalla lo decide UBA_ROL_PERMISO (pantalla Perfiles).
      { path: 'gui',                component: UbaGuiComponent,            canActivate: [permisoGuard('inicio')] },
      { path: 'usuario',            component: UsuarioComponent,           canActivate: [permisoGuard('mis-denuncias')] },
      { path: 'registrar-denuncia', component: RegistrarDenunciaComponent, canActivate: [permisoGuard('registrar-denuncia', 'CREAR')] },
      { path: 'control-plagas',     component: ControlPlagasComponent,     canActivate: [permisoGuard('control-plagas')] },

      // Ficha de seguimiento: la ve cualquiera con sesion, incluido el
      // ciudadano. /reginspeccion es el formulario de trabajo del tecnico y
      // exige ese rol, asi que no servia para esto.
      { path: 'denuncia/:id',       component: DetalleDenunciaComponent },

      { path: 'tecnico',       component: TecnicoComponent,       canActivate: [permisoGuard('inspecciones')] },
      { path: 'reginspeccion', component: RegInspeccionComponent, canActivate: [permisoGuard('inspecciones', 'CREAR')] },

      // Informe tecnico: lo abren tecnico y administracion; sin CREAR queda en solo lectura.
      { path: 'informe-inspeccion/:id', component: InformeInspeccionComponent, canActivate: [permisoGuard('inspecciones')] },

      { path: 'gestionar-usuarios', component: GestionarUserComponent, canActivate: [permisoGuard('usuarios')] },
      { path: 'gestionar-tecnicos', component: GestionarTecComponent,  canActivate: [permisoGuard('tecnicos')] },
      { path: 'catalogos',          component: CatalogosComponent,     canActivate: [permisoGuard('catalogos')] },
      { path: 'perfiles',           component: PerfilesComponent,      canActivate: [permisoGuard('perfiles')] }
    ]
  },

  { path: '**', redirectTo: 'login' }
];
