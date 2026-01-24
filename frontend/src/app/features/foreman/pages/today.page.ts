import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ForemanApi,
  ApiError,
  ForemanCrewSummary,
  TodayBoardResponse,
} from '../data-access/foreman.api';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-foreman-today-page',
  imports: [
    ReactiveFormsModule,
    PageHeaderComponent,
    EmptyStateComponent,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './today.page.html',
  styleUrl: './today.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForemanTodayPage {
  private readonly foremanApi = inject(ForemanApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly crews = signal<ForemanCrewSummary[]>([]);
  readonly board = signal<TodayBoardResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);

  readonly filterForm = this.formBuilder.nonNullable.group({
    crewId: ['', [Validators.required]],
    date: [this.today(), [Validators.required]],
  });

  constructor() {
    this.loadCrews();
  }

  loadCrews(): void {
    this.loading.set(true);
    this.foremanApi.getCrews().subscribe({
      next: (crews) => {
        this.crews.set(crews);
        if (!this.filterForm.controls.crewId.value && crews.length > 0) {
          this.filterForm.controls.crewId.setValue(crews[0].crewId);
        }
        this.loading.set(false);
        if (this.filterForm.valid) {
          this.loadBoard();
        }
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  loadBoard(): void {
    if (this.filterForm.invalid) {
      this.filterForm.markAllAsTouched();
      return;
    }
    const { crewId, date } = this.filterForm.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.foremanApi.getToday(date, crewId).subscribe({
      next: (board) => {
        this.board.set(board);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  private today(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }
}
