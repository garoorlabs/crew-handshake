import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthSessionService } from '../../../core/auth/auth-session.service';
import { ApiError, AuthApi, MeResponse } from '../data-access/auth.api';

@Component({
  selector: 'app-auth-login-page',
  imports: [ReactiveFormsModule],
  templateUrl: './login.page.html',
  styleUrl: './login.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthLoginPage {
  private readonly authApi = inject(AuthApi);
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(FormBuilder);

  readonly step = signal<'phone' | 'code'>('phone');
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly phoneValue = signal('');

  readonly phoneForm = this.formBuilder.nonNullable.group({
    phone: ['', [Validators.required]]
  });

  readonly codeForm = this.formBuilder.nonNullable.group({
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  readonly isPhoneInvalid = computed(() => {
    const control = this.phoneForm.controls.phone;
    return control.touched && control.invalid;
  });

  readonly isCodeInvalid = computed(() => {
    const control = this.codeForm.controls.code;
    return control.touched && control.invalid;
  });

  readonly phoneServerError = computed(() => {
    const error = this.error();
    return error?.category === 'Validation' ? error.fieldErrors?.['phone'] ?? null : null;
  });

  readonly codeServerError = computed(() => {
    const error = this.error();
    return error?.category === 'Validation' ? error.fieldErrors?.['code'] ?? null : null;
  });

  readonly showTopError = computed(() => {
    const error = this.error();
    return !!error && error.category !== 'Validation';
  });

  readonly phoneErrorMessage = computed(() => {
    if (this.phoneServerError()) {
      return this.phoneServerError();
    }
    return this.isPhoneInvalid() ? 'Enter a valid phone number.' : null;
  });

  readonly codeErrorMessage = computed(() => {
    if (this.codeServerError()) {
      return this.codeServerError();
    }
    return this.isCodeInvalid() ? 'Enter the 6-digit code.' : null;
  });

  readonly isPhoneInvalidState = computed(() => this.isPhoneInvalid() || !!this.phoneServerError());
  readonly isCodeInvalidState = computed(() => this.isCodeInvalid() || !!this.codeServerError());

  readonly phoneDescribedBy = computed(() =>
    this.isPhoneInvalidState() ? 'phone-help phone-error' : 'phone-help'
  );

  readonly codeDescribedBy = computed(() =>
    this.isCodeInvalidState() ? 'code-help code-error' : 'code-help'
  );

  private readonly demoPhones = {
    admin: '+14155550100',
    foreman: '+14155550101'
  };

  onSendCode(): void {
    if (this.phoneForm.invalid) {
      this.phoneForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const phone = this.phoneForm.controls.phone.value;
    this.authApi.startOtp(phone).subscribe({
      next: (response) => {
        this.phoneValue.set(response.phoneE164);
        this.step.set('code');
        this.loading.set(false);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  onVerifyCode(): void {
    if (this.codeForm.invalid) {
      this.codeForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const phone = this.phoneValue();
    const code = this.codeForm.controls.code.value;
    this.authApi.verifyOtp(phone, code).subscribe({
      next: (me) => {
        this.handleLoginSuccess(me);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  onDemoLogin(role: 'admin' | 'foreman'): void {
    this.loading.set(true);
    this.error.set(null);
    const phone = this.demoPhones[role];
    this.authApi.devLogin(phone).subscribe({
      next: (me) => {
        this.handleLoginSuccess(me);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  onOpenWorkerDemo(): void {
    this.loading.set(true);
    this.error.set(null);
    this.authApi.devWorkerLink().subscribe({
      next: (response) => {
        this.loading.set(false);
        window.location.href = response.url;
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }

  onEditPhone(): void {
    this.step.set('phone');
    this.codeForm.reset();
    this.error.set(null);
  }

  private handleLoginSuccess(me: MeResponse): void {
    this.session.applySession(me);
    this.session.refresh().subscribe({
      next: (updated) => {
        this.loading.set(false);
        if (!updated.activeCompanyId) {
          this.router.navigate(['/auth/company']);
          return;
        }
        this.router.navigate([this.session.getDefaultRoute()]);
      },
      error: (error: ApiError) => {
        this.error.set(error);
        this.loading.set(false);
      }
    });
  }
}
