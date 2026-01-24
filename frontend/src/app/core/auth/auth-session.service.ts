import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize, Observable, tap } from 'rxjs';
import { ApiError } from '../../features/auth/data-access/auth.api';
import { AuthApi, MeResponse } from '../../features/auth/data-access/auth.api';

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  readonly me = signal<MeResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<ApiError | null>(null);
  readonly isAuthenticated = computed(() => !!this.me());
  readonly hasActiveCompany = computed(() => !!this.me()?.activeCompanyId);
  private readonly authApi = inject(AuthApi);

  refresh(): Observable<MeResponse> {
    this.loading.set(true);
    this.error.set(null);
    return this.authApi.getMe().pipe(
      tap((me) => this.me.set(me)),
      finalize(() => this.loading.set(false)),
      tap({
        error: (error: ApiError) => {
          this.me.set(null);
          this.error.set(error);
        }
      })
    );
  }

  setActiveCompany(companyId: string): Observable<MeResponse> {
    this.loading.set(true);
    this.error.set(null);
    return this.authApi.setActiveCompany(companyId).pipe(
      tap((me) => this.me.set(me)),
      finalize(() => this.loading.set(false)),
      tap({
        error: (error: ApiError) => {
          this.error.set(error);
        }
      })
    );
  }

  logout(): Observable<void> {
    this.loading.set(true);
    this.error.set(null);
    return this.authApi.logout().pipe(
      tap(() => this.me.set(null)),
      finalize(() => this.loading.set(false)),
      tap({
        error: (error: ApiError) => {
          this.error.set(error);
        }
      })
    );
  }

  getDefaultRoute(): string {
    const roles = this.me()?.activeRoles ?? [];
    if (roles.includes('ADMIN')) {
      return '/a';
    }
    return '/f';
  }

  clearLocalSession(): void {
    this.me.set(null);
  }

  applySession(me: MeResponse): void {
    this.me.set(me);
  }
}
