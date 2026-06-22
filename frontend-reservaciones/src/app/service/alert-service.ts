import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';
import type { SweetAlertIcon } from 'sweetalert2';

@Injectable({ providedIn: 'root' })
export class AlertService {
  success(title: string, text = '') {
    return Swal.fire({
      icon: 'success',
      title,
      text,
      confirmButtonText: 'Aceptar',
      confirmButtonColor: '#1d3554'
    });
  }

  error(title: string, text = '') {
    return Swal.fire({
      icon: 'error',
      title,
      text,
      confirmButtonText: 'Aceptar',
      confirmButtonColor: '#1d3554'
    });
  }

  warning(title: string, text = '') {
    return Swal.fire({
      icon: 'warning',
      title,
      text,
      confirmButtonText: 'Aceptar',
      confirmButtonColor: '#1d3554'
    });
  }

  info(title: string, text = '') {
    return Swal.fire({
      icon: 'info',
      title,
      text,
      confirmButtonText: 'Aceptar',
      confirmButtonColor: '#1d3554'
    });
  }

  async confirm(options: {
    title: string;
    text?: string;
    confirmButtonText?: string;
    icon?: SweetAlertIcon;
  }): Promise<boolean> {
    const result = await Swal.fire({
      icon: options.icon ?? 'question',
      title: options.title,
      text: options.text ?? '',
      showCancelButton: true,
      confirmButtonText: options.confirmButtonText ?? 'Sí, continuar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#1d3554',
      cancelButtonColor: '#6c757d',
      reverseButtons: true
    });

    return result.isConfirmed;
  }

  loading(title = 'Procesando...', text = 'Espera un momento') {
    return Swal.fire({
      title,
      text,
      allowOutsideClick: false,
      allowEscapeKey: false,
      didOpen: () => Swal.showLoading()
    });
  }

  close(): void {
    Swal.close();
  }
}
