export interface CuartoDTO {
  id: number;
  tipo: string;
  numero: number;
  precio: number;
  numeroCamas: number;
  disponible: boolean;
}

export interface CreateCuartoDTO {
  tipo: string;
  numero: number;
  precio: number;
  numeroCamas: number;
}

export interface UpdateCuartoDTO {
  tipo: string;
  numero: number;
  precio: number;
  numeroCamas: number;
}

export interface UpdateDisponibilidadDTO {
  disponible: boolean;
}

export type EstadoReservacion = 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA' | 'FINALIZADA';

export interface ReservacionDTO {
  idReservacion: number;
  idCuarto: number;
  numeroCuarto: number;
  tipoCuarto: string;
  nombreCliente: string;
  correoCliente: string;
  telefonoCliente: string;
  fechaEntrada: string;
  fechaSalida: string;
  numeroHuespedes: number;
  precioNoche: number;
  total: number;
  estado: EstadoReservacion;
  observaciones: string;
  fechaCreacion: string;
  tieneIdentificacion: boolean;
}

export interface CreateReservacionDTO {
  idCuarto: number;
  nombreCliente: string;
  correoCliente: string;
  telefonoCliente: string;
  fechaEntrada: string;
  fechaSalida: string;
  numeroHuespedes: number;
  observaciones: string;
}

export type RolUsuario = 'ADMIN' | 'USUARIO';

export interface UsuarioDTO {
  idUsuario: number;
  nombre: string;
  correo: string;
  rol: RolUsuario;
  activo: boolean;
}

export interface RegisterRequestDTO {
  nombre: string;
  correo: string;
  password: string;
}

export interface LoginCredentials {
  correo: string;
  password: string;
}
