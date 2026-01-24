import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ForemanApi, ApiError, CrewCallResponse, CrewCallSummaryResponse, ForemanCrewSummary, SiteSummary } from '../data-access/foreman.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-foreman-crew-calls-page',
  imports: [ReactiveFormsModule, EmptyStateComponent, PageHeaderComponent, LoadingSpinnerComponent, StatusBadgeComponent],
  templateUrl: './crew-calls.page.html',
  styleUrl: './crew-calls.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForemanCrewCallsPage {
  private readonly foremanApi = inject(ForemanApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly crews = signal<ForemanCrewSummary[]>([]);
  readonly sites = signal<SiteSummary[]>([]);
  readonly crewCalls = signal<CrewCallSummaryResponse[]>([]);
  readonly lastSend = signal<CrewCallResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly selectedCrewCallId = signal<string | null>(null);

  readonly filterForm = this.formBuilder.nonNullable.group({
    crewId: ['', [Validators.required]],
    date: [this.today(), [Validators.required]]
  });

  readonly sendForm = this.formBuilder.nonNullable.group({
    crewId: ['', [Validators.required]],
    siteId: ['', [Validators.required]],
    startAt: ['', [Validators.required]],
    meetPoint: ['', [Validators.required]]
  });

  readonly isResend = computed(() => !!this.selectedCrewCallId());
  readonly submitLabel = computed(() => (this.isResend() ? 'Resend crew call' : 'Send crew call'));

  constructor() {
    this.loadReferenceData();
  }

  loadReferenceData(): void {
    this.loading.set(true);
    this.error.set(null);
    this.foremanApi.getCrews().subscribe({
      next: (crews) => {
        this.crews.set(crews);
        if (crews.length > 0) {
          const crewId = crews[0].crewId;
          this.filterForm.controls.crewId.setValue(crewId);
          this.sendForm.controls.crewId.setValue(crewId);
          this.loadCrewCalls();
        }
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
    this.foremanApi.getSites().subscribe({
      next: (sites) => this.sites.set(sites),
      error: () => {}
    });
  }

  loadCrewCalls(): void {
    if (this.filterForm.invalid) {
      this.filterForm.markAllAsTouched();
      return;
    }
    const { crewId, date } = this.filterForm.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.foremanApi.getCrewCalls(date, crewId).subscribe({
      next: (calls) => {
        this.crewCalls.set(calls);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  onSelectCall(call: CrewCallSummaryResponse): void {
    this.selectedCrewCallId.set(call.crewCallId);
    this.sendForm.patchValue({
      crewId: this.filterForm.controls.crewId.value,
      startAt: call.startAt ? this.toLocalInput(call.startAt) : '',
      meetPoint: call.meetPoint
    });
  }

  clearSelection(): void {
    this.selectedCrewCallId.set(null);
    this.sendForm.patchValue({
      startAt: '',
      meetPoint: ''
    });
  }

  onSend(): void {
    if (this.sendForm.invalid) {
      this.sendForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const payload = this.sendForm.getRawValue();
    const request = {
      crewId: payload.crewId,
      siteId: payload.siteId,
      startAt: new Date(payload.startAt).toISOString(),
      meetPoint: payload.meetPoint
    };

    if (this.isResend() && this.selectedCrewCallId()) {
      this.foremanApi.resendCrewCall(this.selectedCrewCallId()!, request).subscribe({
        next: (response) => {
          this.lastSend.set(response);
          this.loading.set(false);
          this.loadCrewCalls();
        },
        error: (error: ApiError) => {
          this.error.set(error);
          this.loading.set(false);
        }
      });
      return;
    }

    this.foremanApi.createCrewCall(request).subscribe({
      next: (response) => {
        this.lastSend.set(response);
        this.loading.set(false);
        this.loadCrewCalls();
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
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
