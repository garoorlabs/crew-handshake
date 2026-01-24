import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AdminApi, ApiError, PayrollSummaryResponse } from '../data-access/admin.api';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-payroll-page',
  imports: [PageHeaderComponent, LoadingSpinnerComponent],
  templateUrl: './payroll.page.html',
  styleUrl: './payroll.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPayrollPage {
  private readonly adminApi = inject(AdminApi);

  readonly summary = signal<PayrollSummaryResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly exporting = signal(false);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.adminApi.getPayrollSummary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  onExport(): void {
    const summary = this.summary();
    if (!summary) {
      return;
    }
    this.exporting.set(true);
    this.adminApi.exportPayroll(summary.periodId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `payroll-${summary.periodId}.csv`;
        anchor.click();
        URL.revokeObjectURL(url);
        this.exporting.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.exporting.set(false);
      },
    });
  }
}
