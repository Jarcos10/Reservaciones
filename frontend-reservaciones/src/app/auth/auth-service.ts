import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap, timeout } from 'rxjs';
import { environment } from '../../environments/environment';
import { RegisterRequestDTO, RolUsuario, UsuarioDTO } from '../model/models';

const AUTH_HEADER_KEY = 'reservaciones.auth.header';
const AUTH_USER_KEY = 'reservaciones.auth.user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly tiempoEspera = 10000;

  readonly usuario = signal<UsuarioDTO | null>(this.leerUsuarioGuardado());
  readonly authHeader = signal<string | null>(sessionStorage.getItem(AUTH_HEADER_KEY));

  readonly autenticado = computed(() => Boolean(this.usuario() && this.authHeader()));
  readonly esAdmin = computed(() => this.usuario()?.rol === 'ADMIN');
  readonly esUsuario = computed(() => this.usuario()?.rol === 'USUARIO');
  readonly puedeReservar = computed(() => this.esUsuario());

  login(correo: string, password: string): Observable<UsuarioDTO> {
    const authorization = `Basic ${this.codificarCredenciales(correo, password)}`;
    const headers = new HttpHeaders({ Authorization: authorization });

    return this.http
      .get<UsuarioDTO>(`${this.baseUrl}/auth/login`, { headers })
      .pipe(
        timeout(this.tiempoEspera),
        tap(usuario => this.guardarSesion(usuario, authorization))
      );
  }

  registrar(dto: RegisterRequestDTO): Observable<UsuarioDTO> {
    return this.http
      .post<UsuarioDTO>(`${this.baseUrl}/auth/registro`, dto)
      .pipe(timeout(this.tiempoEspera));
  }

  consultarSesion(): Observable<UsuarioDTO> {
    return this.http
      .get<UsuarioDTO>(`${this.baseUrl}/auth/me`)
      .pipe(
        timeout(this.tiempoEspera),
        tap(usuario => {
          const header = this.authHeader();
          if (header) {
            this.guardarSesion(usuario, header);
          }
        })
      );
  }

  cerrarSesion(): void {
    sessionStorage.removeItem(AUTH_HEADER_KEY);
    sessionStorage.removeItem(AUTH_USER_KEY);
    this.authHeader.set(null);
    this.usuario.set(null);
  }

  tieneRol(rolesPermitidos: RolUsuario[]): boolean {
    const rolActual = this.usuario()?.rol;
    return Boolean(rolActual && rolesPermitidos.includes(rolActual));
  }

  rutaInicialPorRol(): string {
    return this.esAdmin() ? '/admin' : '/reservar';
  }

  private guardarSesion(usuario: UsuarioDTO, authorization: string): void {
    sessionStorage.setItem(AUTH_HEADER_KEY, authorization);
    sessionStorage.setItem(AUTH_USER_KEY, JSON.stringify(usuario));
    this.authHeader.set(authorization);
    this.usuario.set(usuario);
  }

  private leerUsuarioGuardado(): UsuarioDTO | null {
    const raw = sessionStorage.getItem(AUTH_USER_KEY);

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as UsuarioDTO;
    } catch {
      sessionStorage.removeItem(AUTH_USER_KEY);
      return null;
    }
  }

  private codificarCredenciales(correo: string, password: string): string {
    const valor = `${correo.trim()}:${password}`;
    const bytes = new TextEncoder().encode(valor);
    let binario = '';

    for (const byte of bytes) {
      binario += String.fromCharCode(byte);
    }

    return btoa(binario);
  }
}
