// consulta.service.ts

import { Injectable } from '@angular/core';

import {
  HttpClient,
  HttpHeaders
} from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})

export class ConsultaService {

  // =========================================
  // URL BASE API
  // =========================================

  private apiUrl = environment.url_api;

  constructor(
    private http: HttpClient
  ) { }

  // =========================================
  // HEADERS
  // =========================================

  private headers = new HttpHeaders({
    'Content-Type': 'application/json'
  });

  // =========================================
  // LOGIN
  // =========================================

  login(data: any): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/login`,
      data,
      { headers: this.headers }
    );
  }

  // =========================================
  // OBTENER TÉCNICOS
  // =========================================

  obtenerTecnicos(): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/tecnicos`
    );
  }

  // =========================================
  // OBTENER TÉCNICO POR ID
  // =========================================

  obtenerTecnicoPorId(id: number): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/tecnicos/${id}`
    );
  }

  // =========================================
  // GUARDAR TÉCNICO
  // =========================================

  guardarTecnico(data: any): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/tecnicos`,
      data,
      { headers: this.headers }
    );
  }

  // =========================================
  // ACTUALIZAR TÉCNICO
  // =========================================

  actualizarTecnico(
    id: number,
    data: any
  ): Observable<any> {

    return this.http.put(
      `${this.apiUrl}/tecnicos/${id}`,
      data,
      { headers: this.headers }
    );
  }

  // =========================================
  // ELIMINAR TÉCNICO
  // =========================================

  eliminarTecnico(id: number): Observable<any> {

    return this.http.delete(
      `${this.apiUrl}/tecnicos/${id}`
    );
  }

  // =========================================
  // OBTENER TRÁMITES
  // =========================================

  obtenerTramites(): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/tramites`
    );
  }

  // =========================================
  // OBTENER TRÁMITE POR ID
  // =========================================

  obtenerTramitePorId(id: number): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/tramites/${id}`
    );
  }

  // =========================================
  // GUARDAR INSPECCIÓN
  // =========================================

  guardarInspeccion(data: any): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/inspecciones`,
      data,
      { headers: this.headers }
    );
  }

  // =========================================
  // SUBIR IMÁGENES
  // =========================================

  subirImagenes(
    formData: FormData
  ): Observable<any> {

    return this.http.post(
      `${this.apiUrl}/imagenes`,
      formData
    );
  }

  // =========================================
  // OBTENER ÁREAS
  // =========================================

  obtenerAreas(): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/areas`
    );
  }

  // =========================================
  // OBTENER MOTIVOS
  // =========================================

  obtenerMotivos(): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/motivos`
    );
  }

  // =========================================
  // OBTENER RODENTICIDAS
  // =========================================

  obtenerRodenticidas(): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/rodenticidas`
    );
  }

  // =========================================
  // OBTENER NIVELES
  // =========================================

  obtenerNiveles(
    especie: number
  ): Observable<any> {

    return this.http.get(
      `${this.apiUrl}/niveles/${especie}`
    );
  }

}