import { Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../auth/auth-service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  readonly authService = inject(AuthService);
  readonly menuAbierto = signal(false);
  readonly rutaActual = signal('');

  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  constructor() {
    this.rutaActual.set(this.router.url);

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(event => {
        this.rutaActual.set(event.urlAfterRedirects);
        this.menuAbierto.set(false);
      });
  }

  @HostListener('document:click', ['$event'])
  cerrarMenuAlDarClickFuera(event: MouseEvent): void {
    const target = event.target as Node | null;

    if (this.menuAbierto() && target && !this.elementRef.nativeElement.contains(target)) {
      this.menuAbierto.set(false);
    }
  }

  @HostListener('document:keydown.escape')
  cerrarMenuConEscape(): void {
    this.menuAbierto.set(false);
  }

  ocultarHeader(): boolean {
    const ruta = this.rutaActual();
    return ruta.startsWith('/login') || ruta.startsWith('/registro');
  }

  tituloVista(): string {
    const ruta = this.rutaActual();

    if (ruta.startsWith('/admin')) {
      return 'Administración';
    }

    if (ruta.startsWith('/reservar')) {
      return 'Reservar habitación';
    }

    if (ruta.startsWith('/no-autorizado')) {
      return 'Acceso restringido';
    }

    return 'ReservaHotel';
  }

  subtituloVista(): string {
    const ruta = this.rutaActual();

    if (ruta.startsWith('/admin')) {
      return 'Panel de control';
    }

    if (ruta.startsWith('/reservar')) {
      return 'Disponibilidad y solicitud';
    }

    if (ruta.startsWith('/no-autorizado')) {
      return 'Permisos insuficientes';
    }

    return 'Sistema de reservaciones';
  }

  alternarMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuAbierto.update(valor => !valor);
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  inicialUsuario(): string {
    const nombre = this.authService.usuario()?.nombre?.trim();
    return nombre ? nombre.charAt(0).toUpperCase() : 'U';
  }

  cerrarSesion(): void {
    this.menuAbierto.set(false);
    this.authService.cerrarSesion();
    void this.router.navigateByUrl('/login');
  }
}
