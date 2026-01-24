import { ChangeDetectionStrategy, Component, OnInit, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthSessionService } from '../../../core/auth/auth-session.service';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';

@Component({
  selector: 'app-auth-company-select-page',
  imports: [EmptyStateComponent],
  templateUrl: './company-select.page.html',
  styleUrl: './company-select.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthCompanySelectPage implements OnInit {
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);

  readonly me = this.session.me;
  readonly loading = this.session.loading;
  readonly error = this.session.error;
  readonly memberships = computed(() => this.me()?.memberships ?? []);

  ngOnInit(): void {
    this.session.refresh().subscribe({
      next: () => {
        if (this.session.hasActiveCompany()) {
          this.router.navigate([this.session.getDefaultRoute()]);
        }
      },
      error: () => {
        this.router.navigate(['/auth/login']);
      },
    });
  }

  onSelectCompany(companyId: string): void {
    this.session.setActiveCompany(companyId).subscribe({
      next: () => {
        this.router.navigate([this.session.getDefaultRoute()]);
      },
      error: () => {},
    });
  }
}
