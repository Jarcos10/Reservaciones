import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthService } from './auth-service';

export const basicAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const authHeader = authService.authHeader();

  const esApiReservaciones = req.url.startsWith(environment.apiUrl);
  const yaTieneAuthorization = req.headers.has('Authorization');

  if (!esApiReservaciones || !authHeader || yaTieneAuthorization) {
    return next(req);
  }

  return next(req.clone({
    setHeaders: {
      Authorization: authHeader
    }
  }));
};
