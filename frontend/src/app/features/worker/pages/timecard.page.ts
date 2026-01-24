import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { WorkerApi, ApiError, WorkerTimecardEntry, WorkerTimecardResponse } from '../data-access/worker.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-worker-timecard-page',
  imports: [ReactiveFormsModule, EmptyStateComponent, LoadingSpinnerComponent, StatusBadgeComponent],
  templateUrl: './timecard.page.html',
  styleUrl: './timecard.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkerTimecardPage {
  private readonly route = inject(ActivatedRoute);
  private readonly workerApi = inject(WorkerApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly token = signal<string | null>(null);
  readonly data = signal<WorkerTimecardResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly selectedEntry = signal<WorkerTimecardEntry | null>(null);

  readonly reviewForm = this.formBuilder.nonNullable.group({
    workDate: ['', [Validators.required]],
    reason: ['', [Validators.required]],
    note: ['']
  });

  readonly canSubmitReview = computed(() => this.reviewForm.valid && !!this.selectedEntry());

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const token = params.get('token');
      if (token) {
        this.token.set(token);
        this.loadTimecard();
      }
    });
  }

  loadTimecard(): void {
    const token = this.token();
    if (!token) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.workerApi.getTimecard(token).subscribe({
      next: (response) => {
        this.data.set(response);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  selectEntry(entry: WorkerTimecardEntry): void {
    this.selectedEntry.set(entry);
    this.reviewForm.reset({
      workDate: entry.workDate,
      reason: '',
      note: ''
    });
  }

  submitReview(): void {
    const token = this.token();
    if (!token || this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }
    const payload = this.reviewForm.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.workerApi.submitReviewRequest(token, {
      workDate: payload.workDate,
      reason: payload.reason,
      note: payload.note
    }).subscribe({
      next: () => {
        this.selectedEntry.set(null);
        this.loadTimecard();
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }
}
