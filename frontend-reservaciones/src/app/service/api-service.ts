import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, timeout } from 'rxjs';

import {
  CreateCuartoDTO,
  CuartoDTO,
  CreateReservacionDTO,
  ReservacionDTO,
  UpdateCuartoDTO,
  UpdateDisponibilidadDTO
} from '../model/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl = environment.apiUrl;
  private readonly tiempoEspera = 10000;

  constructor(private readonly http: HttpClient) {}

  obtenerCuartos(): Observable<CuartoDTO[]> {
    return this.http
      .get<CuartoDTO[]>(`${this.baseUrl}/cuartos`)
      .pipe(timeout(this.tiempoEspera));
  }

  obtenerCuartosDisponibles(fechaEntrada: string, fechaSalida: string): Observable<CuartoDTO[]> {
    const params = new HttpParams()
      .set('fechaEntrada', fechaEntrada)
      .set('fechaSalida', fechaSalida);

    return this.http
      .get<CuartoDTO[]>(`${this.baseUrl}/cuartos/disponibles`, { params })
      .pipe(timeout(this.tiempoEspera));
  }

  crearCuarto(dto: CreateCuartoDTO): Observable<CuartoDTO> {
    return this.http
      .post<CuartoDTO>(`${this.baseUrl}/cuartos`, dto)
      .pipe(timeout(this.tiempoEspera));
  }

  editarCuarto(id: number, dto: UpdateCuartoDTO): Observable<CuartoDTO> {
    return this.http
      .put<CuartoDTO>(`${this.baseUrl}/cuartos/${id}`, dto)
      .pipe(timeout(this.tiempoEspera));
  }

  eliminarCuarto(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/cuartos/${id}`)
      .pipe(timeout(this.tiempoEspera));
  }

  actualizarDisponibilidad(id: number, dto: UpdateDisponibilidadDTO): Observable<CuartoDTO> {
    return this.http
      .patch<CuartoDTO>(`${this.baseUrl}/cuartos/${id}/disponibilidad`, dto)
      .pipe(timeout(this.tiempoEspera));
  }

  obtenerReservaciones(): Observable<ReservacionDTO[]> {
    return this.http
      .get<ReservacionDTO[]>(`${this.baseUrl}/reservaciones`)
      .pipe(timeout(this.tiempoEspera));
  }

  crearReservacion(dto: CreateReservacionDTO): Observable<ReservacionDTO> {
    return this.http
      .post<ReservacionDTO>(`${this.baseUrl}/reservaciones`, dto)
      .pipe(timeout(this.tiempoEspera));
  }

  crearReservacionConIdentificacion(dto: CreateReservacionDTO, archivo: File): Observable<ReservacionDTO> {
    const formData = new FormData();

    formData.append(
      'datos',
      new Blob([JSON.stringify(dto)], { type: 'application/json' })
    );

    formData.append('archivo', archivo);

    return this.http
      .post<ReservacionDTO>(`${this.baseUrl}/reservaciones/con-identificacion`, formData)
      .pipe(timeout(this.tiempoEspera));
  }

  confirmarReservacion(id: number): Observable<ReservacionDTO> {
    return this.http
      .patch<ReservacionDTO>(`${this.baseUrl}/reservaciones/${id}/confirmar`, {})
      .pipe(timeout(this.tiempoEspera));
  }

  cancelarReservacion(id: number): Observable<ReservacionDTO> {
    return this.http
      .patch<ReservacionDTO>(`${this.baseUrl}/reservaciones/${id}/cancelar`, {})
      .pipe(timeout(this.tiempoEspera));
  }

  finalizarReservacion(id: number): Observable<ReservacionDTO> {
    return this.http
      .patch<ReservacionDTO>(`${this.baseUrl}/reservaciones/${id}/finalizar`, {})
      .pipe(timeout(this.tiempoEspera));
  }

  descargarIdentificacion(idReservacion: number): Observable<Blob> {
    return this.http
      .get(`${this.baseUrl}/archivos/reservaciones/${idReservacion}/identificacion`, {
        responseType: 'blob'
      })
      .pipe(timeout(this.tiempoEspera));
  }

  urlDescargarIdentificacion(idReservacion: number): string {
    return `${this.baseUrl}/archivos/reservaciones/${idReservacion}/identificacion`;
  }
}
