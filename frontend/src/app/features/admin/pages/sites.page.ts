import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminApi, ApiError, SiteResponse } from '../data-access/admin.api';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-admin-sites-page',
  imports: [
    ReactiveFormsModule,
    EmptyStateComponent,
    PageHeaderComponent,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './sites.page.html',
  styleUrl: './sites.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSitesPage {
  private readonly adminApi = inject(AdminApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly sites = signal<SiteResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly editingId = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required]],
    address: [''],
    notes: [''],
    active: [true],
  });

  readonly isEditing = computed(() => !!this.editingId());
  readonly formTitle = computed(() => (this.isEditing() ? 'Edit site' : 'Add site'));
  readonly submitLabel = computed(() => (this.isEditing() ? 'Save site' : 'Add site'));

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.adminApi.getSites().subscribe({
      next: (sites) => {
        this.sites.set(sites);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  onEdit(site: SiteResponse): void {
    this.editingId.set(site.siteId);
    this.form.reset({
      name: site.name,
      address: site.address ?? '',
      notes: site.notes ?? '',
      active: site.active,
    });
  }

  onCancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({
      name: '',
      address: '',
      notes: '',
      active: true,
    });
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
      this.adminApi
        .updateSite({
          siteId: this.editingId()!,
          name: payload.name,
          address: payload.address,
          notes: payload.notes,
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
      .createSite({
        name: payload.name,
        address: payload.address,
        notes: payload.notes,
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

  trackById(index: number, site: SiteResponse): string {
    return site.siteId;
  }
}
