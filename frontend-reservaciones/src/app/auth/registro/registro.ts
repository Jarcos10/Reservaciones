import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize, switchMap } from 'rxjs';
import { AuthService } from '../auth-service';
import { AlertService } from '../../service/alert-service';
import { RegisterRequestDTO } from '../../model/models';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css'
})
export class RegistroComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly alertas = inject(AlertService);
  private readonly router = inject(Router);

  readonly cargando = signal(false);
  readonly error = signal('');

  readonly registroForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    correo: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(60)]],
    confirmarPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(60)]]
  });

  registrar(): void {
    if (this.registroForm.invalid) {
      this.error.set('Completa correctamente los datos de registro.');
      void this.alertas.warning('Datos incompletos', this.error());
      return;
    }

    const { nombre, correo, password, confirmarPassword } = this.registroForm.getRawValue();

    if (password !== confirmarPassword) {
      this.error.set('Las contraseñas no coinciden.');
      void this.alertas.warning('Contraseñas diferentes', this.error());
      return;
    }

    const dto: RegisterRequestDTO = { nombre, correo, password };

    this.cargando.set(true);
    this.error.set('');

    this.authService.registrar(dto)
      .pipe(
        switchMap(() => this.authService.login(correo, password)),
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: usuario => {
          void this.alertas.success(
            'Cuenta creada',
            `${usuario.nombre}, tu cuenta fue creada correctamente con rol USUARIO.`
          );

          void this.router.navigateByUrl('/reservar');
        },
        error: err => {
          const mensaje = this.extraerMensajeError(err);
          this.error.set(mensaje);
          void this.alertas.error('No se pudo crear la cuenta', mensaje);
        }
      });
  }

  private extraerMensajeError(err: unknown): string {
    const errorResponse = err as {
      status?: number;
      name?: string;
      error?: { messaege?: string; message?: string; details?: string } | string;
      message?: string;
    };

    if (errorResponse.status === 409) {
      return 'Ya existe un usuario registrado con ese correo.';
    }

    if (errorResponse.status === 400) {
      return 'Los datos enviados no cumplen las validaciones del backend.';
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
      ?? 'Ocurrió un error inesperado al registrar la cuenta.';
  }
}
