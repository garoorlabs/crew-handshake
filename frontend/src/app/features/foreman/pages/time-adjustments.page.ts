import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
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

@Component({
  selector: 'app-foreman-time-adjustments-page',
  imports: [ReactiveFormsModule, PageHeaderComponent, EmptyStateComponent, LoadingSpinnerComponent],
  templateUrl: './time-adjustments.page.html',
  styleUrl: './time-adjustments.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForemanTimeAdjustmentsPage {
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

  readonly adjustForm = this.formBuilder.nonNullable.group({
    timeEntryId: ['', [Validators.required]],
    checkInAt: ['', [Validators.required]],
    checkOutAt: ['', [Validators.required]],
    reason: ['', [Validators.required]],
    note: [''],
  });

  readonly adjustableEntries = computed(() =>
    (this.board()?.workers ?? []).filter((worker) => !!worker.timeEntryId),
  );

  constructor() {
    this.loadCrews();
  }

  loadCrews(): void {
    this.loading.set(true);
    this.foremanApi.getCrews().subscribe({
      next: (crews) => {
        this.crews.set(crews);
        if (crews.length > 0) {
          this.filterForm.controls.crewId.setValue(crews[0].crewId);
          this.loadBoard();
        }
        this.loading.set(false);
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

  onSelectEntry(entryId: string): void {
    const entry = this.adjustableEntries().find((worker) => worker.timeEntryId === entryId);
    if (!entry) {
      return;
    }
    this.adjustForm.patchValue({
      timeEntryId: entry.timeEntryId ?? '',
      checkInAt: entry.checkInAt ? this.toLocalInput(entry.checkInAt) : '',
      checkOutAt: entry.checkOutAt ? this.toLocalInput(entry.checkOutAt) : '',
    });
  }

  onSubmit(): void {
    if (this.adjustForm.invalid) {
      this.adjustForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const payload = this.adjustForm.getRawValue();
    this.foremanApi
      .adjustTime({
        timeEntryId: payload.timeEntryId,
        checkInAt: new Date(payload.checkInAt).toISOString(),
        checkOutAt: new Date(payload.checkOutAt).toISOString(),
        reason: payload.reason,
        note: payload.note,
      })
      .subscribe({
        next: () => {
          this.loadBoard();
        },
        error: (error: ApiError) => {
          this.error.set(error);
          this.loading.set(false);
        },
      });
  }

  private toLocalInput(value: string): string {
    const date = new Date(value);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  private today(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }
}
