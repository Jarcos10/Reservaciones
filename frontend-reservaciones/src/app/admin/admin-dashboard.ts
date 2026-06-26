import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, of, timeout } from 'rxjs';

import { ApiService } from '../service/api-service';
import { AlertService } from '../service/alert-service';
import { CuartoDTO, ReservacionDTO } from '../model/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit {
  private readonly api = inject(ApiService);
  private readonly alertas = inject(AlertService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  cuartos: CuartoDTO[] = [];
  reservaciones: ReservacionDTO[] = [];

  mensaje = '';
  error = '';

  cargandoCuartos = false;
  cargandoReservaciones = false;

  idCuartoEditando: number | null = null;

  readonly cuartoForm = this.fb.nonNullable.group({
    tipo: ['', [Validators.required, Validators.minLength(4)]],
    numero: [0, [Validators.required, Validators.min(1)]],
    precio: [0, [Validators.required, Validators.min(1)]],
    numeroCamas: [1, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.cargarDatos();
  }

  get cargando(): boolean {
    return this.cargandoCuartos || this.cargandoReservaciones;
  }

  get estaEditando(): boolean {
    return this.idCuartoEditando !== null;
  }

  get cuartosDisponibles(): number {
    return this.cuartos.filter(cuarto => cuarto.disponible).length;
  }

  get cuartosFueraServicio(): number {
    return this.cuartos.filter(cuarto => !cuarto.disponible).length;
  }

  get reservacionesPendientes(): number {
    return this.reservaciones.filter(reservacion => reservacion.estado === 'PENDIENTE').length;
  }

  get reservacionesConPdf(): number {
    return this.reservaciones.filter(reservacion => reservacion.tieneIdentificacion).length;
  }

  cargarDatos(): void {
    this.mensaje = '';
    this.error = '';

    this.cargarCuartos();
    this.cargarReservaciones();
    this.actualizarVista();
  }

  cargarCuartos(): void {
    //console.log('Entró a cargarCuartos()');

    this.cargandoCuartos = true;
    this.actualizarVista();

    this.api.obtenerCuartos()
      .pipe(
        timeout(10000),
        catchError(err => {
          //console.error('Error al cargar cuartos:', err);

          this.error = 'No se pudieron cargar los cuartos. Verifica que el backend esté activo.';

          void this.alertas.error(
            'Error al cargar cuartos',
            this.error
          );

          return of([] as CuartoDTO[]);
        }),
        finalize(() => {
          //console.log('Finalizó cargarCuartos()');

          this.cargandoCuartos = false;
          this.actualizarVista();
        })
      )
      .subscribe(cuartos => {
        //console.log('Cuartos recibidos:', cuartos);

        this.cuartos = [...cuartos].sort((a, b) => a.numero - b.numero);
        this.actualizarVista();
      });
  }

  cargarReservaciones(): void {
    //console.log('Entró a cargarReservaciones()');

    this.cargandoReservaciones = true;
    this.actualizarVista();

    this.api.obtenerReservaciones()
      .pipe(
        timeout(10000),
        catchError(err => {
          console.error('Error al cargar reservaciones:', err);

          this.error = 'No se pudieron cargar las reservaciones. Verifica el endpoint de reservaciones.';

          void this.alertas.warning(
            'Reservaciones no disponibles',
            this.error
          );

          return of([] as ReservacionDTO[]);
        }),
        finalize(() => {
          //console.log('Finalizó cargarReservaciones()');

          this.cargandoReservaciones = false;
          this.actualizarVista();
        })
      )
      .subscribe(reservaciones => {
        //console.log('Reservaciones recibidas:', reservaciones);

        this.reservaciones = [...reservaciones].sort(
          (a, b) => b.idReservacion - a.idReservacion
        );

        this.actualizarVista();
      });
  }

  guardarCuarto(): void {
    if (this.cuartoForm.invalid) {
      this.mostrarAdvertencia(
        'Formulario incompleto',
        'Completa correctamente los datos del cuarto.'
      );
      return;
    }

    const dto = this.cuartoForm.getRawValue();

    if (this.estaEditando && this.idCuartoEditando !== null) {
      this.cargandoCuartos = true;
      this.actualizarVista();

      this.api.editarCuarto(this.idCuartoEditando, dto)
        .pipe(
          timeout(10000),
          finalize(() => {
            this.cargandoCuartos = false;
            this.actualizarVista();
          })
        )
        .subscribe({
          next: cuartoActualizado => {
            this.cuartos = this.cuartos
              .map(cuarto => cuarto.id === this.idCuartoEditando ? cuartoActualizado : cuarto)
              .sort((a, b) => a.numero - b.numero);

            this.mensaje = 'Cuarto actualizado correctamente.';
            this.limpiarFormularioCuarto();

            this.actualizarVista();

            void this.alertas.success(
              'Cuarto actualizado',
              'La información del cuarto se guardó correctamente.'
            );

            this.cargarCuartos();
          },
          error: err => this.mostrarError(err)
        });

      return;
    }

    this.cargandoCuartos = true;
    this.actualizarVista();

    this.api.crearCuarto(dto)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargandoCuartos = false;
          this.actualizarVista();
        })
      )
      .subscribe({
        next: cuartoCreado => {
          this.cuartos = [...this.cuartos, cuartoCreado]
            .sort((a, b) => a.numero - b.numero);

          this.mensaje = 'Cuarto registrado correctamente.';
          this.limpiarFormularioCuarto();

          this.actualizarVista();

          void this.alertas.success(
            'Cuarto registrado',
            'El cuarto se agregó correctamente al sistema.'
          );

          this.cargarCuartos();
        },
        error: err => this.mostrarError(err)
      });
  }

  editarCuarto(cuarto: CuartoDTO): void {
    this.idCuartoEditando = cuarto.id;
    this.mensaje = '';
    this.error = '';

    this.cuartoForm.patchValue({
      tipo: cuarto.tipo,
      numero: cuarto.numero,
      precio: cuarto.precio,
      numeroCamas: cuarto.numeroCamas
    });

    this.actualizarVista();

    document
      .getElementById('formulario-cuarto')
      ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  cancelarEdicion(): void {
    this.limpiarFormularioCuarto();
    this.mensaje = '';
    this.error = '';
    this.actualizarVista();
  }

  async eliminarCuarto(cuarto: CuartoDTO): Promise<void> {
    const confirmado = await this.alertas.confirm({
      title: `Eliminar cuarto ${cuarto.numero}`,
      text: 'Solo se podrá eliminar si no tiene reservaciones asociadas. Esta acción no se puede deshacer.',
      confirmButtonText: 'Eliminar',
      icon: 'warning'
    });

    if (!confirmado) {
      return;
    }

    this.cargandoCuartos = true;
    this.actualizarVista();

    this.api.eliminarCuarto(cuarto.id)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargandoCuartos = false;
          this.actualizarVista();
        })
      )
      .subscribe({
        next: () => {
          this.cuartos = this.cuartos.filter(item => item.id !== cuarto.id);

          if (this.idCuartoEditando === cuarto.id) {
            this.limpiarFormularioCuarto();
          }

          this.mensaje = `Cuarto ${cuarto.numero} eliminado correctamente.`;
          this.actualizarVista();

          void this.alertas.success(
            'Cuarto eliminado',
            `El cuarto ${cuarto.numero} fue eliminado correctamente.`
          );

          this.cargarCuartos();
        },
        error: err => this.mostrarError(err)
      });
  }

  cambiarDisponibilidad(cuarto: CuartoDTO): void {
    const nuevoEstado = !cuarto.disponible;

    this.cargandoCuartos = true;
    this.actualizarVista();

    this.api.actualizarDisponibilidad(cuarto.id, { disponible: nuevoEstado })
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargandoCuartos = false;
          this.actualizarVista();
        })
      )
      .subscribe({
        next: cuartoActualizado => {
          this.cuartos = this.cuartos
            .map(item => item.id === cuarto.id ? cuartoActualizado : item)
            .sort((a, b) => a.numero - b.numero);

          this.mensaje = `Disponibilidad del cuarto ${cuarto.numero} actualizada.`;
          this.actualizarVista();

          void this.alertas.success(
            'Disponibilidad actualizada',
            `El cuarto ${cuarto.numero} quedó ${nuevoEstado ? 'habilitado' : 'fuera de servicio'}.`
          );

          this.cargarCuartos();
        },
        error: err => this.mostrarError(err)
      });
  }

  confirmar(id: number): void {
    this.cargandoReservaciones = true;
    this.actualizarVista();

    this.api.confirmarReservacion(id)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargandoReservaciones = false;
          this.actualizarVista();
        })
      )
      .subscribe({
        next: () => {
          void this.alertas.success(
            'Reservación confirmada',
            `La reservación #${id} fue confirmada.`
          );

          this.cargarReservaciones();
        },
        error: err => this.mostrarError(err)
      });
  }

  async cancelar(id: number): Promise<void> {
    const confirmado = await this.alertas.confirm({
      title: `Cancelar reservación #${id}`,
      text: 'La reservación quedará marcada como cancelada.',
      confirmButtonText: 'Cancelar reservación',
      icon: 'warning'
    });

    if (!confirmado) {
      return;
    }

    this.cargandoReservaciones = true;
    this.actualizarVista();

    this.api.cancelarReservacion(id)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargandoReservaciones = false;
          this.actualizarVista();
        })
      )
      .subscribe({
        next: () => {
          void this.alertas.success(
            'Reservación cancelada',
            `La reservación #${id} fue cancelada.`
          );

          this.cargarReservaciones();
        },
        error: err => this.mostrarError(err)
      });
  }

  finalizar(id: number): void {
    this.cargandoReservaciones = true;
    this.actualizarVista();

    this.api.finalizarReservacion(id)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargandoReservaciones = false;
          this.actualizarVista();
        })
      )
      .subscribe({
        next: () => {
          void this.alertas.success(
            'Reservación finalizada',
            `La reservación #${id} fue finalizada.`
          );

          this.cargarReservaciones();
        },
        error: err => this.mostrarError(err)
      });
  }

  descargarIdentificacion(id: number): void {
    this.cargandoReservaciones = true;
    this.actualizarVista();

    this.api.descargarIdentificacion(id)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.cargandoReservaciones = false;
          this.actualizarVista();
        })
      )
      .subscribe({
        next: archivo => this.guardarArchivo(archivo, `identificacion-reservacion-${id}.pdf`),
        error: err => this.mostrarError(err)
      });
  }

  private guardarArchivo(archivo: Blob, nombre: string): void {
    const url = URL.createObjectURL(archivo);
    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = nombre;
    enlace.click();

    URL.revokeObjectURL(url);
  }

  private limpiarFormularioCuarto(): void {
    this.idCuartoEditando = null;

    this.cuartoForm.reset({
      tipo: '',
      numero: 0,
      precio: 0,
      numeroCamas: 1
    });
  }

  private mostrarAdvertencia(titulo: string, mensaje: string): void {
    this.error = mensaje;
    this.actualizarVista();

    void this.alertas.warning(titulo, mensaje);
  }

  private mostrarError(err: unknown): void {
    this.cargandoCuartos = false;
    this.cargandoReservaciones = false;

    const mensaje = this.extraerMensajeError(err);
    this.error = mensaje;

    this.actualizarVista();

    void this.alertas.error(
      'Ocurrió un error',
      mensaje
    );
  }

  private extraerMensajeError(err: unknown): string {
    const errorResponse = err as {
      name?: string;
      error?: {
        messaege?: string;
        message?: string;
        details?: string;
      } | string;
      message?: string;
    };

    if (errorResponse.name === 'TimeoutError') {
      return 'El backend tardó demasiado en responder. Revisa que Spring Boot y MySQL estén funcionando correctamente.';
    }

    if (typeof errorResponse.error === 'string') {
      return errorResponse.error;
    }

    return errorResponse.error?.messaege
      ?? errorResponse.error?.message
      ?? errorResponse.error?.details
      ?? errorResponse.message
      ?? 'Ocurrió un error inesperado.';
  }

  private actualizarVista(): void {
    this.cdr.detectChanges();
  }
}