import { Routes } from '@angular/router';
import { AdminDashboard } from './admin/admin-dashboard';
import { ReservaHabitaciones } from './reserva-habitaciones/reserva-habitaciones';

export const routes: Routes = [
  { path: '', redirectTo: 'reservar', pathMatch: 'full' },
  { path: 'reservar', component: ReservaHabitaciones },
  { path: 'admin', component: AdminDashboard },
  { path: '**', redirectTo: 'reservar' }
];
