import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ForemanApi, ApiError, ForemanCrewSummary, RollCallRequest, RollCallStatus, TodayBoardResponse } from '../data-access/foreman.api';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-foreman-roll-call-page',
  imports: [ReactiveFormsModule, PageHeaderComponent, EmptyStateComponent, LoadingSpinnerComponent],
  templateUrl: './roll-call.page.html',
  styleUrl: './roll-call.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForemanRollCallPage {
  private readonly foremanApi = inject(ForemanApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly crews = signal<ForemanCrewSummary[]>([]);
  readonly board = signal<TodayBoardResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly entries = signal<Record<string, RollCallStatus>>({});

  readonly filterForm = this.formBuilder.nonNullable.group({
    crewId: ['', [Validators.required]],
    date: [this.today(), [Validators.required]]
  });

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
      }
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
        const next: Record<string, RollCallStatus> = {};
        board.workers.forEach((worker) => {
          next[worker.membershipId] = 'PRESENT';
        });
        this.entries.set(next);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  setStatus(workerId: string, status: RollCallStatus): void {
    this.entries.update((current) => ({ ...current, [workerId]: status }));
  }

  submitRollCall(): void {
    if (this.filterForm.invalid) {
      return;
    }
    const { crewId, date } = this.filterForm.getRawValue();
    const entries = Object.entries(this.entries()).map(([workerMembershipId, status]) => ({
      workerMembershipId,
      status
    }));
    const request: RollCallRequest = { crewId, date, entries };
    this.loading.set(true);
    this.error.set(null);
    this.foremanApi.submitRollCall(request).subscribe({
      next: () => {
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  private today(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }
}
