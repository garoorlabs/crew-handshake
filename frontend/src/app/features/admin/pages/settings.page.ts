import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminApi, ApiError, SettingsResponse } from '../data-access/admin.api';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { LoadingSpinnerComponent } from '../../../shared/ui/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-admin-settings-page',
  imports: [ReactiveFormsModule, PageHeaderComponent, LoadingSpinnerComponent],
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSettingsPage {
  private readonly adminApi = inject(AdminApi);
  private readonly formBuilder = inject(FormBuilder);

  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    defaultLanguage: ['en', [Validators.required]],
    payrollFrequency: ['WEEKLY', [Validators.required]],
    payrollCutoffDay: ['FRIDAY', [Validators.required]],
    standbyCutoffTime: ['18:00', [Validators.required]],
    dispatchAuthority: ['HYBRID', [Validators.required]],
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.adminApi.getSettings().subscribe({
      next: (settings) => {
        this.applySettings(settings);
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      },
    });
  }

  applySettings(settings: SettingsResponse): void {
    this.form.reset({
      defaultLanguage: settings.defaultLanguage,
      payrollFrequency: settings.payrollFrequency,
      payrollCutoffDay: settings.payrollCutoffDay,
      standbyCutoffTime: settings.standbyCutoffTime,
      dispatchAuthority: settings.dispatchAuthority,
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
    this.adminApi
      .updateSettings({
        defaultLanguage: payload.defaultLanguage,
        payrollFrequency: payload.payrollFrequency as 'WEEKLY' | 'BIWEEKLY',
        payrollCutoffDay: payload.payrollCutoffDay,
        standbyCutoffTime: payload.standbyCutoffTime,
        dispatchAuthority: payload.dispatchAuthority as 'HYBRID',
      })
      .subscribe({
        next: (settings) => {
          this.applySettings(settings);
          this.loading.set(false);
        },
        error: (error: ApiError) => {
          this.error.set(error);
          this.loading.set(false);
        },
      });
  }
}
