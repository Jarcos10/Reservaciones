import { Routes } from '@angular/router';
import { AdminDashboard } from './admin/admin-dashboard';
import { LoginComponent } from './auth/login/login';
import { NoAutorizadoComponent } from './auth/no-autorizado/no-autorizado';
import { RegistroComponent } from './auth/registro/registro';
import { roleGuard } from './auth/role.guard';
import { ReservaHabitaciones } from './reserva-habitaciones/reserva-habitaciones';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroComponent },
  { path: 'no-autorizado', component: NoAutorizadoComponent },
  {
    path: 'reservar',
    component: ReservaHabitaciones,
    canActivate: [roleGuard],
    data: { roles: ['USUARIO'] }
  },
  {
    path: 'admin',
    component: AdminDashboard,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  { path: '**', redirectTo: 'login' }
];
