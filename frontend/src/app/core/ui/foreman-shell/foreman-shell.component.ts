import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthSessionService } from '../../auth/auth-session.service';

@Component({
  selector: 'app-foreman-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './foreman-shell.component.html',
  styleUrl: './foreman-shell.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ds-layout-foreman',
  },
})
export class ForemanShellComponent {
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);

  logout(): void {
    this.session.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth']);
      },
      error: () => {
        this.router.navigate(['/auth']);
      },
    });
  }

  get isLoggingOut(): boolean {
    return this.session.loading();
  }
}
