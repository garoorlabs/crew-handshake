import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AdminApi, ApiError, AuditLogResponse } from '../data-access/admin.api';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-audit-page',
  imports: [PageHeaderComponent, EmptyStateComponent, LoadingSpinnerComponent],
  templateUrl: './audit.page.html',
  styleUrl: './audit.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAuditPage {
  private readonly adminApi = inject(AdminApi);

  readonly logs = signal<AuditLogResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.adminApi.getAuditLogs().subscribe({
      next: (logs) => {
        this.logs.set(logs);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }
}
