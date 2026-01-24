import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminApi, ApiError, ForemanResponse } from '../data-access/admin.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-admin-foremen-page',
  imports: [ReactiveFormsModule, EmptyStateComponent, PageHeaderComponent, LoadingSpinnerComponent, StatusBadgeComponent],
  templateUrl: './foremen.page.html',
  styleUrl: './foremen.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminForemenPage {
  private readonly adminApi = inject(AdminApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly foremen = signal<ForemanResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly editingId = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    displayName: ['', [Validators.required]],
    phone: ['', [Validators.required]],
    active: [true]
  });

  readonly isEditing = computed(() => !!this.editingId());
  readonly formTitle = computed(() => (this.isEditing() ? 'Edit foreman' : 'Add foreman'));
  readonly submitLabel = computed(() => (this.isEditing() ? 'Save changes' : 'Add foreman'));

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.adminApi.getForemen().subscribe({
      next: (foremen) => {
        this.foremen.set(foremen);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  onEdit(foreman: ForemanResponse): void {
    this.editingId.set(foreman.membershipId);
    this.form.reset({
      displayName: foreman.displayName,
      phone: foreman.phoneE164,
      active: foreman.active
    });
    this.form.controls.phone.disable();
  }

  onCancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({
      displayName: '',
      phone: '',
      active: true
    });
    this.form.controls.phone.enable();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const payload = this.form.getRawValue();

    if (this.isEditing() && this.editingId()) {
      this.adminApi.updateForeman({
        membershipId: this.editingId()!,
        displayName: payload.displayName,
        active: payload.active
      }).subscribe({
        next: () => {
          this.onCancelEdit();
          this.load();
        },
        error: (error: ApiError) => {
          this.error.set(error);
          this.loading.set(false);
        }
      });
      return;
    }

    this.adminApi.createForeman({
      displayName: payload.displayName,
      phone: payload.phone,
      active: payload.active
    }).subscribe({
      next: () => {
        this.onCancelEdit();
        this.load();
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  trackById(index: number, foreman: ForemanResponse): string {
    return foreman.membershipId;
  }
}
