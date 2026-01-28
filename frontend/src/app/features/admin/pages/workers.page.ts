import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AdminApi, ApiError, CrewResponse, WorkerResponse } from '../data-access/admin.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { ErrorBannerComponent } from '../../../shared/ui/error-banner/error-banner.component';
import { FieldErrorComponent } from '../../../shared/ui/field-error/field-error.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-admin-workers-page',
  imports: [
    ReactiveFormsModule,
    EmptyStateComponent,
    ErrorBannerComponent,
    FieldErrorComponent,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './workers.page.html',
  styleUrl: './workers.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminWorkersPage {
  private readonly adminApi = inject(AdminApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly workers = signal<WorkerResponse[]>([]);
  readonly crews = signal<CrewResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly loadError = signal<ApiError | null>(null);
  readonly submitError = signal<ApiError | null>(null);
  readonly editingId = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    displayName: ['', [Validators.required]],
    phone: ['', [Validators.required]],
    preferredLanguage: ['en'],
    crewId: [''],
    active: [true],
  });

  readonly isEditing = computed(() => !!this.editingId());
  readonly isBusy = computed(() => this.loading() || this.saving());
  readonly formTitle = computed(() => (this.isEditing() ? 'Edit worker' : 'Add worker'));
  readonly submitLabel = computed(() => {
    if (this.saving()) {
      return this.isEditing() ? 'Saving worker...' : 'Adding worker...';
    }
    return this.isEditing() ? 'Save worker' : 'Add worker';
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.submitError.set(null);
    forkJoin({
      workers: this.adminApi.getWorkers(),
      crews: this.adminApi.getCrews(),
    }).subscribe({
      next: ({ workers, crews }) => {
        this.workers.set(workers);
        this.crews.set(crews);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.loadError.set(error);
        this.loading.set(false);
      },
    });
  }

  onEdit(worker: WorkerResponse): void {
    this.clearServerErrors();
    this.submitError.set(null);
    this.editingId.set(worker.membershipId);
    this.form.reset({
      displayName: worker.displayName,
      phone: worker.phoneE164,
      preferredLanguage: worker.preferredLanguage,
      crewId: worker.crewId ?? '',
      active: worker.active,
    });
    this.form.controls.phone.disable();
  }

  onCancelEdit(): void {
    this.clearServerErrors();
    this.submitError.set(null);
    this.editingId.set(null);
    this.form.reset({
      displayName: '',
      phone: '',
      preferredLanguage: 'en',
      crewId: '',
      active: true,
    });
    this.form.controls.phone.enable();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.clearServerErrors();
    this.saving.set(true);
    this.submitError.set(null);
    const payload = this.form.getRawValue();
    const crewId = payload.crewId ? payload.crewId : null;

    if (this.isEditing() && this.editingId()) {
      this.adminApi
        .updateWorker({
          membershipId: this.editingId()!,
          displayName: payload.displayName,
          preferredLanguage: payload.preferredLanguage,
          crewId,
          active: payload.active,
        })
        .subscribe({
          next: () => {
            this.saving.set(false);
            this.onCancelEdit();
            this.load();
          },
          error: (error: ApiError) => {
            if (!this.applyServerErrors(error)) {
              this.submitError.set(error);
            }
            this.saving.set(false);
          },
        });
      return;
    }

    this.adminApi
      .createWorker({
        displayName: payload.displayName,
        phone: payload.phone,
        preferredLanguage: payload.preferredLanguage,
        crewId,
        active: payload.active,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.onCancelEdit();
          this.load();
        },
        error: (error: ApiError) => {
          if (!this.applyServerErrors(error)) {
            this.submitError.set(error);
          }
          this.saving.set(false);
        },
      });
  }

  trackById(index: number, worker: WorkerResponse): string {
    return worker.membershipId;
  }

  nameError(): string {
    return this.controlErrorMessage(this.form.controls.displayName, 'Name is required.');
  }

  nameErrorId(): string | null {
    return this.nameError() ? 'worker-name-error' : null;
  }

  phoneError(): string {
    return this.controlErrorMessage(this.form.controls.phone, 'Phone is required.');
  }

  phoneErrorId(): string | null {
    return this.phoneError() ? 'worker-phone-error' : null;
  }

  crewError(): string {
    return this.controlErrorMessage(this.form.controls.crewId, '');
  }

  crewErrorId(): string | null {
    return this.crewError() ? 'worker-crew-error' : null;
  }

  private applyServerErrors(error: ApiError): boolean {
    if (error.category !== 'Validation' || !error.fieldErrors) {
      return false;
    }
    Object.entries(error.fieldErrors).forEach(([field, message]) => {
      const control = this.form.get(field);
      if (!control) {
        return;
      }
      const existing = control.errors ?? {};
      control.setErrors({ ...existing, server: message });
    });
    this.form.markAllAsTouched();
    return true;
  }

  private clearServerErrors(): void {
    Object.values(this.form.controls).forEach((control) => {
      const errors = control.errors;
      if (!errors || !('server' in errors)) {
        return;
      }
      const { server, ...rest } = errors;
      control.setErrors(Object.keys(rest).length > 0 ? rest : null);
    });
  }

  private controlErrorMessage(control: AbstractControl, fallback: string): string {
    if (control.errors?.['server']) {
      return String(control.errors['server']);
    }
    if (!control.touched || !control.invalid) {
      return '';
    }
    if (control.errors?.['required']) {
      return fallback;
    }
    return fallback;
  }
}
