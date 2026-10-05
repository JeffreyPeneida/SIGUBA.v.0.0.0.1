import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';

export interface AdminZonal {
  idAdminZonal: number;
  nombre: string;
}

@Injectable({ providedIn: 'root' })
export class AdminZonalApi {

  private readonly api = inject(ApiService);

  listar(): Observable<AdminZonal[]> {
    return this.api.get<AdminZonal[]>('adminzonal/buscarAdministracionesZonales');
  }
}
