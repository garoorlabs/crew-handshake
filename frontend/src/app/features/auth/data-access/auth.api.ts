import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { ApiError, mapHttpError } from '../../../core/http/api-error';

export interface OtpStartResponse {
  phoneE164: string;
}

export interface MeMembership {
  membershipId: string;
  companyId: string;
  companyName: string;
  roles: string[];
}

export interface MeResponse {
  identityId: string;
  phoneE164: string;
  activeCompanyId: string | null;
  activeMembershipId: string | null;
  activeRoles: string[];
  memberships: MeMembership[];
}

export interface DevWorkerLinkResponse {
  url: string;
  token: string;
  crewCallId: string;
  phoneE164: string;
}

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  startOtp(phone: string): Observable<OtpStartResponse> {
    return this.http
      .post<OtpStartResponse>('/api/v1/auth/otp/start', { phone })
      .pipe(catchError((error) => throwError(() => mapHttpError(error))));
  }

  verifyOtp(phone: string, code: string): Observable<MeResponse> {
    return this.http
      .post<MeResponse>('/api/v1/auth/otp/verify', { phone, code })
      .pipe(catchError((error) => throwError(() => mapHttpError(error))));
  }

  getMe(): Observable<MeResponse> {
    return this.http
      .get<MeResponse>('/api/v1/me')
      .pipe(catchError((error) => throwError(() => mapHttpError(error))));
  }

  setActiveCompany(companyId: string): Observable<MeResponse> {
    return this.http
      .post<MeResponse>('/api/v1/me/active-company', { companyId })
      .pipe(catchError((error) => throwError(() => mapHttpError(error))));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>('/api/v1/auth/logout', {})
      .pipe(catchError((error) => throwError(() => mapHttpError(error))));
  }

  devLogin(phone: string): Observable<MeResponse> {
    return this.http
      .post<MeResponse>('/api/v1/auth/dev/login', { phone })
      .pipe(catchError((error) => throwError(() => mapHttpError(error))));
  }

  devWorkerLink(): Observable<DevWorkerLinkResponse> {
    return this.http
      .post<DevWorkerLinkResponse>('/api/v1/auth/dev/worker-link', {})
      .pipe(catchError((error) => throwError(() => mapHttpError(error))));
  }
}

export type { ApiError };
