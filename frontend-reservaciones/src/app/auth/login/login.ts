import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../auth-service';
import { AlertService } from '../../service/alert-service';
import { RolUsuario } from '../../model/models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly alertas = inject(AlertService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly cargando = signal(false);
  readonly error = signal('');

  readonly loginForm = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  iniciarSesion(): void {
    if (this.loginForm.invalid) {
      this.error.set('Ingresa un correo válido y una contraseña de al menos 6 caracteres.');
      void this.alertas.warning('Datos incompletos', this.error());
      return;
    }

    const { correo, password } = this.loginForm.getRawValue();

    this.cargando.set(true);
    this.error.set('');

    this.authService.login(correo, password)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: usuario => {
          void this.alertas.success(
            'Bienvenido',
            `${usuario.nombre}, ingresaste como ${usuario.rol}.`
          );

          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          void this.router.navigateByUrl(this.resolverDestino(returnUrl, usuario.rol));
        },
        error: err => {
          const mensaje = this.extraerMensajeError(err);
          this.error.set(mensaje);
          void this.alertas.error('No se pudo iniciar sesión', mensaje);
        }
      });
  }

  usarAdminDemo(): void {
    this.loginForm.patchValue({
      correo: 'admin@reservahotel.com',
      password: 'Admin12345'
    });
  }

  usarUsuarioDemo(): void {
    this.loginForm.patchValue({
      correo: 'usuario@reservahotel.com',
      password: 'Usuario12345'
    });
  }

  private resolverDestino(returnUrl: string | null, rol: RolUsuario): string {
    if (returnUrl === '/admin' && rol === 'ADMIN') {
      return '/admin';
    }

    if (returnUrl === '/reservar' && rol === 'USUARIO') {
      return '/reservar';
    }

    return rol === 'ADMIN' ? '/admin' : '/reservar';
  }

  private extraerMensajeError(err: unknown): string {
    const errorResponse = err as {
      status?: number;
      name?: string;
      error?: { messaege?: string; message?: string; details?: string } | string;
      message?: string;
    };

    if (errorResponse.status === 401) {
      return 'Correo o contraseña incorrectos. Verifica tus credenciales.';
    }

    if (errorResponse.status === 403) {
      return 'La cuenta no tiene permisos para acceder o se encuentra inactiva.';
    }

    if (errorResponse.name === 'TimeoutError') {
      return 'El backend tardó demasiado en responder. Verifica que Spring Boot esté activo.';
    }

    if (typeof errorResponse.error === 'string') {
      return errorResponse.error;
    }

    return errorResponse.error?.messaege
      ?? errorResponse.error?.message
      ?? errorResponse.error?.details
      ?? errorResponse.message
      ?? 'Ocurrió un error inesperado al iniciar sesión.';
  }
}
