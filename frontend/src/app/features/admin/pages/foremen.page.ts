import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminApi, ApiError, ForemanResponse } from '../data-access/admin.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { ErrorBannerComponent } from '../../../shared/ui/error-banner/error-banner.component';
import { FieldErrorComponent } from '../../../shared/ui/field-error/field-error.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-admin-foremen-page',
  imports: [
    ReactiveFormsModule,
    EmptyStateComponent,
    ErrorBannerComponent,
    FieldErrorComponent,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './foremen.page.html',
  styleUrl: './foremen.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminForemenPage {
  private readonly adminApi = inject(AdminApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly foremen = signal<ForemanResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly loadError = signal<ApiError | null>(null);
  readonly submitError = signal<ApiError | null>(null);
  readonly editingId = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    displayName: ['', [Validators.required]],
    phone: ['', [Validators.required]],
    active: [true],
  });

  readonly isEditing = computed(() => !!this.editingId());
  readonly isBusy = computed(() => this.loading() || this.saving());
  readonly formTitle = computed(() => (this.isEditing() ? 'Edit foreman' : 'Add foreman'));
  readonly submitLabel = computed(() => {
    if (this.saving()) {
      return this.isEditing() ? 'Saving foreman...' : 'Adding foreman...';
    }
    return this.isEditing() ? 'Save foreman' : 'Add foreman';
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.adminApi.getForemen().subscribe({
      next: (foremen) => {
        this.foremen.set(foremen);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.loadError.set(error);
        this.loading.set(false);
      },
    });
  }

  onEdit(foreman: ForemanResponse): void {
    this.clearServerErrors();
    this.submitError.set(null);
    this.editingId.set(foreman.membershipId);
    this.form.reset({
      displayName: foreman.displayName,
      phone: foreman.phoneE164,
      active: foreman.active,
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

    if (this.isEditing() && this.editingId()) {
      this.adminApi
        .updateForeman({
          membershipId: this.editingId()!,
          displayName: payload.displayName,
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
      .createForeman({
        displayName: payload.displayName,
        phone: payload.phone,
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

  trackById(index: number, foreman: ForemanResponse): string {
    return foreman.membershipId;
  }

  nameError(): string {
    return this.controlErrorMessage(this.form.controls.displayName, 'Name is required.');
  }

  nameErrorId(): string | null {
    return this.nameError() ? 'foreman-name-error' : null;
  }

  phoneError(): string {
    return this.controlErrorMessage(this.form.controls.phone, 'Phone is required.');
  }

  phoneErrorId(): string | null {
    return this.phoneError() ? 'foreman-phone-error' : null;
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
