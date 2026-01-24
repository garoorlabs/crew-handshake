import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AdminApi, ApiError, CrewResponse, WorkerResponse } from '../data-access/admin.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-admin-workers-page',
  imports: [
    ReactiveFormsModule,
    EmptyStateComponent,
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
  readonly error = signal<ApiError | null>(null);
  readonly editingId = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    displayName: ['', [Validators.required]],
    phone: ['', [Validators.required]],
    preferredLanguage: ['en'],
    crewId: [''],
    active: [true],
  });

  readonly isEditing = computed(() => !!this.editingId());
  readonly formTitle = computed(() => (this.isEditing() ? 'Edit worker' : 'Add worker'));
  readonly submitLabel = computed(() => (this.isEditing() ? 'Save changes' : 'Add worker'));

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
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
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  onEdit(worker: WorkerResponse): void {
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
    this.loading.set(true);
    this.error.set(null);
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
            this.onCancelEdit();
            this.load();
          },
          error: (error: ApiError) => {
            this.error.set(error);
            this.loading.set(false);
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
          this.onCancelEdit();
          this.load();
        },
        error: (error: ApiError) => {
          this.error.set(error);
          this.loading.set(false);
        },
      });
  }

  trackById(index: number, worker: WorkerResponse): string {
    return worker.membershipId;
  }
}
