import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthSessionService } from '../../auth/auth-session.service';

@Component({
  selector: 'app-admin-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ds-layout-admin',
  },
})
export class AdminShellComponent {
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
