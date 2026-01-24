import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ForemanApi,
  ApiError,
  ExceptionResponse,
  ForemanCrewSummary,
} from '../data-access/foreman.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-foreman-exceptions-page',
  imports: [
    ReactiveFormsModule,
    EmptyStateComponent,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './exceptions.page.html',
  styleUrl: './exceptions.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForemanExceptionsPage {
  private readonly foremanApi = inject(ForemanApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly crews = signal<ForemanCrewSummary[]>([]);
  readonly exceptions = signal<ExceptionResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);

  readonly filterForm = this.formBuilder.nonNullable.group({
    crewId: ['', [Validators.required]],
    date: [this.today(), [Validators.required]],
  });

  readonly resolveForm = this.formBuilder.nonNullable.group({
    exceptionId: ['', [Validators.required]],
    action: ['ADJUST_TIME', [Validators.required]],
    checkInAt: [''],
    checkOutAt: [''],
    reason: [''],
    note: [''],
  });

  readonly selectedException = computed(() => {
    const id = this.resolveForm.controls.exceptionId.value;
    return this.exceptions().find((item) => item.exceptionId === id) ?? null;
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
          this.loadExceptions();
        }
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  loadExceptions(): void {
    if (this.filterForm.invalid) {
      this.filterForm.markAllAsTouched();
      return;
    }
    const { crewId, date } = this.filterForm.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.foremanApi.getExceptions(date, crewId).subscribe({
      next: (items) => {
        this.exceptions.set(items);
        if (items.length > 0) {
          this.resolveForm.controls.exceptionId.setValue(items[0].exceptionId);
        }
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  onResolve(): void {
    if (this.resolveForm.invalid) {
      this.resolveForm.markAllAsTouched();
      return;
    }
    const payload = this.resolveForm.getRawValue();
    const request = {
      action: payload.action as 'APPROVE_AS_IS' | 'ADJUST_TIME' | 'MARK_NO_SHOW',
      checkInAt: payload.checkInAt ? this.toIso(payload.checkInAt) : null,
      checkOutAt: payload.checkOutAt ? this.toIso(payload.checkOutAt) : null,
      reason: payload.reason || null,
      note: payload.note || null,
    };
    this.loading.set(true);
    this.error.set(null);
    this.foremanApi.resolveException(payload.exceptionId, request).subscribe({
      next: () => {
        this.loadExceptions();
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  private toIso(value: string): string {
    return new Date(value).toISOString();
  }

  private today(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }
}
