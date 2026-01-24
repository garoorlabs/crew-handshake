import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { WorkerApi, ApiError, WorkerCrewCallResponse, WorkerAvailabilityRequest } from '../data-access/worker.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-worker-crew-call-page',
  imports: [ReactiveFormsModule, RouterLink, EmptyStateComponent, LoadingSpinnerComponent, StatusBadgeComponent],
  templateUrl: './crew-call.page.html',
  styleUrl: './crew-call.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkerCrewCallPage {
  private readonly route = inject(ActivatedRoute);
  private readonly workerApi = inject(WorkerApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly token = signal<string | null>(null);
  readonly data = signal<WorkerCrewCallResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly etaMinutes = signal(15);
  readonly statusMessage = signal<string | null>(null);

  readonly availabilityForm = this.formBuilder.nonNullable.group({
    availabilityAfter: ['AFTER_9', [Validators.required]],
    differentSiteOk: [false],
    note: ['']
  });

  readonly handshakeLabel = computed(() => {
    const data = this.data();
    const status = data?.handshakeStatus;
    if (!status) {
      return 'Unconfirmed';
    }
    if (status === 'LATE') {
      return data?.lateEtaMinutes ? `Late by ${data.lateEtaMinutes} min` : 'Late';
    }
    if (status === 'CONFIRMED') {
      return 'Confirmed';
    }
    if (status === 'CANT') {
      return "Can't make it";
    }
    if (status === 'NEED_CHANGE') {
      return 'Needs change';
    }
    return status;
  });

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const token = params.get('token');
      if (token) {
        this.token.set(token);
        this.loadCrewCall();
      }
    });
  }

  loadCrewCall(): void {
    const token = this.token();
    if (!token) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.workerApi.getCrewCall(token).subscribe({
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

  submitHandshake(status: 'CONFIRMED' | 'LATE' | 'CANT' | 'NEED_CHANGE'): void {
    const token = this.token();
    if (!token) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const request = status === 'LATE'
      ? { status, lateEtaMinutes: this.etaMinutes() }
      : { status };
    this.workerApi.submitHandshake(token, request).subscribe({
      next: (response) => {
        this.data.set(response);
        this.statusMessage.set('Response recorded.');
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  submitAvailability(): void {
    if (this.availabilityForm.invalid) {
      this.availabilityForm.markAllAsTouched();
      return;
    }
    const token = this.token();
    if (!token) {
      return;
    }
    const payload = this.availabilityForm.getRawValue();
    const availabilityAfter = payload.availabilityAfter as WorkerAvailabilityRequest['availabilityAfter'];
    this.loading.set(true);
    this.error.set(null);
    this.workerApi.submitAvailability(token, {
      availabilityAfter,
      differentSiteOk: payload.differentSiteOk,
      note: payload.note
    }).subscribe({
      next: (response) => {
        this.data.set(response);
        this.statusMessage.set('Availability submitted.');
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  checkIn(): void {
    const token = this.token();
    if (!token) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.workerApi.checkIn(token).subscribe({
      next: () => {
        this.statusMessage.set('Checked in.');
        this.loadCrewCall();
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  checkOut(): void {
    const token = this.token();
    if (!token) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.workerApi.checkOut(token).subscribe({
      next: () => {
        this.statusMessage.set('Checked out.');
        this.loadCrewCall();
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }
}
