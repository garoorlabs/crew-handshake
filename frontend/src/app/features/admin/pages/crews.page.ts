import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  AdminApi,
  ApiError,
  CrewResponse,
  ForemanResponse,
  WorkerResponse,
} from '../data-access/admin.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-crews-page',
  imports: [ReactiveFormsModule, EmptyStateComponent, PageHeaderComponent, LoadingSpinnerComponent],
  templateUrl: './crews.page.html',
  styleUrl: './crews.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminCrewsPage {
  private readonly adminApi = inject(AdminApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly crews = signal<CrewResponse[]>([]);
  readonly foremen = signal<ForemanResponse[]>([]);
  readonly workers = signal<WorkerResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly editingId = signal<string | null>(null);
  readonly selectedWorkerIds = signal<Set<string>>(new Set());

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required]],
    foremanMembershipId: ['', [Validators.required]],
  });

  readonly isEditing = computed(() => !!this.editingId());
  readonly formTitle = computed(() => (this.isEditing() ? 'Edit crew' : 'Create crew'));
  readonly submitLabel = computed(() => (this.isEditing() ? 'Save crew' : 'Create crew'));
  readonly activeWorkers = computed(() => this.workers().filter((worker) => worker.active));

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      crews: this.adminApi.getCrews(),
      foremen: this.adminApi.getForemen(),
      workers: this.adminApi.getWorkers(),
    }).subscribe({
      next: ({ crews, foremen, workers }) => {
        this.crews.set(crews);
        this.foremen.set(foremen);
        this.workers.set(workers);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  toggleWorker(workerId: string): void {
    const next = new Set(this.selectedWorkerIds());
    if (next.has(workerId)) {
      next.delete(workerId);
    } else {
      next.add(workerId);
    }
    this.selectedWorkerIds.set(next);
  }

  onEdit(crew: CrewResponse): void {
    this.editingId.set(crew.crewId);
    this.form.reset({
      name: crew.name,
      foremanMembershipId: crew.foremanMembershipId,
    });
    const next = new Set<string>();
    crew.workers.forEach((worker) => next.add(worker.membershipId));
    this.selectedWorkerIds.set(next);
  }

  onCancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({
      name: '',
      foremanMembershipId: '',
    });
    this.selectedWorkerIds.set(new Set());
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const payload = this.form.getRawValue();
    const workerIds = Array.from(this.selectedWorkerIds());

    if (this.isEditing() && this.editingId()) {
      this.adminApi
        .updateCrew({
          crewId: this.editingId()!,
          name: payload.name,
          foremanMembershipId: payload.foremanMembershipId,
          workerMembershipIds: workerIds,
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
      .createCrew({
        name: payload.name,
        foremanMembershipId: payload.foremanMembershipId,
        workerMembershipIds: workerIds,
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

  trackCrew(index: number, crew: CrewResponse): string {
    return crew.crewId;
  }

  trackWorker(index: number, worker: WorkerResponse): string {
    return worker.membershipId;
  }
}
