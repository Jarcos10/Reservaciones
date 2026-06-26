import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../service/api-service';
import { AlertService } from '../service/alert-service';
import { AuthService } from '../auth/auth-service';
import { CuartoDTO, CreateReservacionDTO } from '../model/models';

interface PisoView {
  piso: number;
  cuartos: CuartoDTO[];
}

interface FechasSeleccionadas {
  fechaEntrada: string;
  fechaSalida: string;
}

@Component({
  selector: 'app-reserva-habitaciones',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe],
  templateUrl: './reserva-habitaciones.html',
  styleUrl: './reserva-habitaciones.css'
})
export class ReservaHabitaciones implements OnInit {
  private readonly api = inject(ApiService);
  private readonly alertas = inject(AlertService);
  private readonly fb = inject(FormBuilder);
  readonly authService = inject(AuthService);

  readonly cuartos = signal<CuartoDTO[]>([]);
  readonly cuartosDisponibles = signal<CuartoDTO[]>([]);
  readonly cuartoSeleccionado = signal<CuartoDTO | null>(null);
  readonly fechasSeleccionadas = signal<FechasSeleccionadas>({ fechaEntrada: '', fechaSalida: '' });
  readonly cargando = signal(false);
  readonly mensaje = signal('');
  readonly error = signal('');
  readonly archivo = signal<File | null>(null);

  readonly filtroForm = this.fb.nonNullable.group({
    fechaEntrada: ['', Validators.required],
    fechaSalida: ['', Validators.required]
  });

  readonly reservacionForm = this.fb.nonNullable.group({
    nombreCliente: ['', [Validators.required, Validators.minLength(3)]],
    correoCliente: ['', [Validators.required, Validators.email]],
    telefonoCliente: ['', [Validators.required, Validators.minLength(8)]],
    numeroHuespedes: [1, [Validators.required, Validators.min(1)]],
    observaciones: ['']
  });

  readonly pisos = computed<PisoView[]>(() => {
    const ordenados = [...this.cuartos()].sort((a, b) => a.numero - b.numero);
    const agrupados = new Map<number, CuartoDTO[]>();

    for (const cuarto of ordenados) {
      const piso = cuarto.numero >= 100 ? Math.floor(cuarto.numero / 100) : 1;
      const lista = agrupados.get(piso) ?? [];
      lista.push(cuarto);
      agrupados.set(piso, lista);
    }

    return [...agrupados.entries()]
      .sort((a, b) => b[0] - a[0])
      .map(([piso, cuartos]) => ({ piso, cuartos }));
  });

  readonly availableIds = computed(() => new Set(this.cuartosDisponibles().map(c => c.id)));
  readonly totalDisponibles = computed(() => this.cuartosDisponibles().length);
  readonly totalOcupados = computed(() => this.cuartos().filter(c => c.disponible && !this.availableIds().has(c.id)).length);
  readonly totalFueraServicio = computed(() => this.cuartos().filter(c => !c.disponible).length);
  readonly nombreArchivoSeleccionado = computed(() => this.archivo()?.name ?? 'Sin archivo seleccionado');

  readonly nochesSeleccionadas = computed(() => {
    const { fechaEntrada, fechaSalida } = this.fechasSeleccionadas();

    if (!fechaEntrada || !fechaSalida) {
      return 0;
    }

    const entrada = this.crearFechaLocal(fechaEntrada);
    const salida = this.crearFechaLocal(fechaSalida);
    const diferencia = salida.getTime() - entrada.getTime();

    return diferencia > 0 ? Math.round(diferencia / 86_400_000) : 0;
  });

  readonly totalEstimado = computed(() => {
    const cuarto = this.cuartoSeleccionado();
    return cuarto ? cuarto.precio * this.nochesSeleccionadas() : 0;
  });

  ngOnInit(): void {
    const hoy = new Date();
    const manana = new Date(hoy);
    manana.setDate(hoy.getDate() + 1);

    this.filtroForm.patchValue({
      fechaEntrada: this.formatearFecha(hoy),
      fechaSalida: this.formatearFecha(manana)
    });

    const usuario = this.authService.usuario();

    if (usuario) {
      this.reservacionForm.patchValue({
        nombreCliente: usuario.nombre,
        correoCliente: usuario.correo
      });
    }

    this.sincronizarFechas();

    this.filtroForm.valueChanges.subscribe(() => {
      this.sincronizarFechas();
    });

    this.cargarCuartos();
  }

  cargarCuartos(): void {
    this.cargando.set(true);

    this.api.obtenerCuartos().subscribe({
      next: (cuartos) => {
        this.cuartos.set([...cuartos].sort((a, b) => a.numero - b.numero));
        this.buscarDisponibles();
      },
      error: (err) => this.mostrarError(err)
    });
  }

  buscarDisponibles(): void {
    this.sincronizarFechas();

    const { fechaEntrada, fechaSalida } = this.fechasSeleccionadas();

    if (!fechaEntrada || !fechaSalida) {
      this.mostrarAdvertencia('Fechas incompletas', 'Selecciona la fecha de entrada y salida.');
      return;
    }

    if (this.nochesSeleccionadas() <= 0) {
      this.mostrarAdvertencia('Rango de fechas no válido', 'La fecha de salida debe ser posterior a la fecha de entrada.');
      return;
    }

    this.error.set('');
    this.mensaje.set('');
    this.cargando.set(true);

    this.api.obtenerCuartosDisponibles(fechaEntrada, fechaSalida).subscribe({
      next: (cuartos) => {
        this.cuartosDisponibles.set(cuartos);
        const actual = this.cuartoSeleccionado();

        if (actual && !this.availableIds().has(actual.id)) {
          this.cuartoSeleccionado.set(null);
        }

        this.cargando.set(false);
      },
      error: (err) => this.mostrarError(err)
    });
  }

  seleccionarCuarto(cuarto: CuartoDTO): void {
    if (!cuarto.disponible || !this.availableIds().has(cuarto.id)) {
      this.mostrarAdvertencia('Habitación no disponible', 'Selecciona una habitación marcada como disponible.');
      return;
    }

    this.cuartoSeleccionado.set(cuarto);
    this.mensaje.set('');
    this.error.set('');
  }

  estadoCuarto(cuarto: CuartoDTO): 'disponible' | 'ocupado' | 'inhabilitado' | 'seleccionado' {
    if (this.cuartoSeleccionado()?.id === cuarto.id) return 'seleccionado';
    if (!cuarto.disponible) return 'inhabilitado';
    if (!this.availableIds().has(cuarto.id)) return 'ocupado';
    return 'disponible';
  }

  textoEstadoCuarto(cuarto: CuartoDTO): string {
    const estado = this.estadoCuarto(cuarto);
    if (estado === 'seleccionado') return 'Seleccionada';
    if (estado === 'inhabilitado') return 'No habilitada';
    if (estado === 'ocupado') return 'Ocupada';
    return 'Disponible';
  }

  seleccionarArchivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;

    if (!archivo) {
      this.archivo.set(null);
      return;
    }

    const nombrePdf = archivo.name.toLowerCase().endsWith('.pdf');
    const tipoPdf = archivo.type === 'application/pdf' || archivo.type === '';

    if (!nombrePdf || !tipoPdf) {
      input.value = '';
      this.archivo.set(null);
      this.mostrarAdvertencia('Archivo no válido', 'La identificación debe ser un archivo PDF.');
      return;
    }

    this.archivo.set(archivo);
  }

  reservar(): void {
    const cuarto = this.cuartoSeleccionado();
    const archivo = this.archivo();
    const { fechaEntrada, fechaSalida } = this.fechasSeleccionadas();

    if (!cuarto) {
      this.mostrarAdvertencia('Selecciona una habitación', 'Debes elegir una habitación disponible para continuar.');
      return;
    }

    if (this.nochesSeleccionadas() <= 0) {
      this.mostrarAdvertencia('Rango de fechas no válido', 'La fecha de salida debe ser posterior a la fecha de entrada.');
      return;
    }

    if (this.reservacionForm.invalid || !fechaEntrada || !fechaSalida) {
      this.mostrarAdvertencia('Datos incompletos', 'Completa correctamente los datos de la reservación.');
      return;
    }

    if (!archivo) {
      this.mostrarAdvertencia('Identificación requerida', 'La identificación en PDF es obligatoria para reservar.');
      return;
    }

    const dto: CreateReservacionDTO = {
      idCuarto: cuarto.id,
      fechaEntrada,
      fechaSalida,
      ...this.reservacionForm.getRawValue()
    };

    this.cargando.set(true);

    this.api.crearReservacionConIdentificacion(dto, archivo).subscribe({
      next: (reservacion) => {
        this.mensaje.set(`Reservación #${reservacion.idReservacion} creada correctamente para la habitación ${cuarto.numero}.`);
        this.error.set('');
        this.archivo.set(null);
        this.reservacionForm.reset({
          nombreCliente: '',
          correoCliente: '',
          telefonoCliente: '',
          numeroHuespedes: 1,
          observaciones: ''
        });
        this.cargando.set(false);
        void this.alertas.success(
          'Reservación creada',
          `Tu reservación #${reservacion.idReservacion} para la habitación ${cuarto.numero} fue registrada correctamente.`
        );
        this.buscarDisponibles();
      },
      error: (err) => this.mostrarError(err)
    });
  }

  private sincronizarFechas(): void {
    const { fechaEntrada, fechaSalida } = this.filtroForm.getRawValue();
    this.fechasSeleccionadas.set({ fechaEntrada, fechaSalida });
  }

  private crearFechaLocal(valor: string): Date {
    const [year, month, day] = valor.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  private formatearFecha(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private mostrarAdvertencia(titulo: string, mensaje: string): void {
    this.error.set(mensaje);
    void this.alertas.warning(titulo, mensaje);
  }

  private mostrarError(err: unknown): void {
    this.cargando.set(false);
    const mensaje = this.extraerMensajeError(err);
    this.error.set(mensaje);
    void this.alertas.error('Ocurrió un error', mensaje);
  }

  private extraerMensajeError(err: unknown): string {
    const errorResponse = err as { error?: { messaege?: string; message?: string; details?: string } | string; message?: string };

    if (typeof errorResponse.error === 'string') {
      return errorResponse.error;
    }

    return errorResponse.error?.messaege
      ?? errorResponse.error?.message
      ?? errorResponse.error?.details
      ?? errorResponse.message
      ?? 'Ocurrió un error inesperado.';
  }
}
