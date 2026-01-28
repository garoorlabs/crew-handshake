import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
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
  readonly menuOpen = signal(false);

  logout(): void {
    this.session.logout().subscribe({
      next: () => {
        this.menuOpen.set(false);
        this.router.navigate(['/auth']);
      },
      error: () => {
        this.menuOpen.set(false);
        this.router.navigate(['/auth']);
      },
    });
  }

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  get isLoggingOut(): boolean {
    return this.session.loading();
  }
}
