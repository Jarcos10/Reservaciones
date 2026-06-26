import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth-service';

@Component({
  selector: 'app-no-autorizado',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './no-autorizado.html',
  styleUrl: './no-autorizado.css'
})
export class NoAutorizadoComponent {
  readonly authService = inject(AuthService);
}
