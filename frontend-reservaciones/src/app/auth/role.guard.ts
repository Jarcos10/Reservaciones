import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { RolUsuario } from '../model/models';
import { AuthService } from './auth-service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.autenticado()) {
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url }
    });
  }

  const rolesPermitidos = route.data['roles'] as RolUsuario[] | undefined;

  if (!rolesPermitidos || authService.tieneRol(rolesPermitidos)) {
    return true;
  }

  return router.createUrlTree(['/no-autorizado']);
};
