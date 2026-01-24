import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { ApiError, mapHttpError } from '../../../core/http/api-error';

export interface WorkerResponse {
  membershipId: string;
  displayName: string;
  phoneE164: string;
  preferredLanguage: string;
  active: boolean;
  crewId: string | null;
  crewName: string | null;
}

export interface WorkerCreateRequest {
  displayName: string;
  phone: string;
  preferredLanguage?: string;
  crewId?: string | null;
  active?: boolean;
}

export interface WorkerUpdateRequest {
  membershipId: string;
  displayName: string;
  preferredLanguage?: string;
  crewId?: string | null;
  active?: boolean;
}

export interface ForemanResponse {
  membershipId: string;
  displayName: string;
  phoneE164: string;
  active: boolean;
}

export interface ForemanCreateRequest {
  displayName: string;
  phone: string;
  active?: boolean;
}

export interface ForemanUpdateRequest {
  membershipId: string;
  displayName: string;
  active?: boolean;
}

export interface CrewWorkerSummary {
  membershipId: string;
  displayName: string;
}

export interface CrewResponse {
  crewId: string;
  name: string;
  foremanMembershipId: string;
  foremanName: string;
  workerCount: number;
  workers: CrewWorkerSummary[];
}

export interface CrewCreateRequest {
  name: string;
  foremanMembershipId: string;
  workerMembershipIds: string[];
}

export interface CrewUpdateRequest {
  crewId: string;
  name: string;
  foremanMembershipId: string;
  workerMembershipIds: string[];
}

export interface SiteResponse {
  siteId: string;
  name: string;
  address: string | null;
  notes: string | null;
  active: boolean;
}

export interface SiteCreateRequest {
  name: string;
  address?: string | null;
  notes?: string | null;
  active?: boolean;
}

export interface SiteUpdateRequest {
  siteId: string;
  name: string;
  address?: string | null;
  notes?: string | null;
  active?: boolean;
}

export interface SettingsResponse {
  defaultLanguage: string;
  payrollFrequency: 'WEEKLY' | 'BIWEEKLY';
  payrollCutoffDay: string;
  standbyCutoffTime: string;
  dispatchAuthority: 'HYBRID';
}

export interface SettingsUpdateRequest {
  defaultLanguage: string;
  payrollFrequency: 'WEEKLY' | 'BIWEEKLY';
  payrollCutoffDay: string;
  standbyCutoffTime: string;
  dispatchAuthority: 'HYBRID';
}

export interface ExceptionResponse {
  exceptionId: string;
  type: string;
  status: string;
  crewId: string;
  crewName: string;
  workerMembershipId: string;
  workerName: string;
  timeEntryId: string | null;
  reviewRequestId: string | null;
  checkInAt: string | null;
  checkOutAt: string | null;
  reviewReason: string | null;
  reviewNote: string | null;
}

export interface ExceptionResolveRequest {
  action: 'APPROVE_AS_IS' | 'ADJUST_TIME' | 'MARK_NO_SHOW';
  checkInAt?: string | null;
  checkOutAt?: string | null;
  reason?: string | null;
  note?: string | null;
}

export interface AuditLogResponse {
  auditId: string;
  actionType: string;
  entityType: string;
  entityId: string;
  actorName: string;
  createdAt: string;
  detailsJson: string | null;
}

export interface PayrollSummaryResponse {
  periodId: string;
  periodStart: string;
  periodEnd: string;
  totalEntries: number;
  unresolvedExceptions: number;
}

export interface PayrollEntryResponse {
  timeEntryId: string;
  workerMembershipId: string;
  workerName: string;
  workDate: string;
  checkInAt: string | null;
  checkOutAt: string | null;
  status: string;
  edited: boolean;
}

export interface PayrollPeriodResponse extends PayrollSummaryResponse {
  entries: PayrollEntryResponse[];
}

@Injectable({ providedIn: 'root' })
export class AdminApi {
  private readonly http = inject(HttpClient);

  getWorkers(): Observable<WorkerResponse[]> {
    return this.http.get<WorkerResponse[]>('/api/v1/admin/workers').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  createWorker(request: WorkerCreateRequest): Observable<WorkerResponse> {
    return this.http.post<WorkerResponse>('/api/v1/admin/workers', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  updateWorker(request: WorkerUpdateRequest): Observable<WorkerResponse> {
    return this.http.put<WorkerResponse>('/api/v1/admin/workers', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getForemen(): Observable<ForemanResponse[]> {
    return this.http.get<ForemanResponse[]>('/api/v1/admin/foremen').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  createForeman(request: ForemanCreateRequest): Observable<ForemanResponse> {
    return this.http.post<ForemanResponse>('/api/v1/admin/foremen', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  updateForeman(request: ForemanUpdateRequest): Observable<ForemanResponse> {
    return this.http.put<ForemanResponse>('/api/v1/admin/foremen', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getCrews(): Observable<CrewResponse[]> {
    return this.http.get<CrewResponse[]>('/api/v1/admin/crews').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  createCrew(request: CrewCreateRequest): Observable<CrewResponse> {
    return this.http.post<CrewResponse>('/api/v1/admin/crews', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  updateCrew(request: CrewUpdateRequest): Observable<CrewResponse> {
    return this.http.put<CrewResponse>('/api/v1/admin/crews', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getSites(): Observable<SiteResponse[]> {
    return this.http.get<SiteResponse[]>('/api/v1/admin/sites').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  createSite(request: SiteCreateRequest): Observable<SiteResponse> {
    return this.http.post<SiteResponse>('/api/v1/admin/sites', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  updateSite(request: SiteUpdateRequest): Observable<SiteResponse> {
    return this.http.put<SiteResponse>('/api/v1/admin/sites', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getSettings(): Observable<SettingsResponse> {
    return this.http.get<SettingsResponse>('/api/v1/admin/settings').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  updateSettings(request: SettingsUpdateRequest): Observable<SettingsResponse> {
    return this.http.put<SettingsResponse>('/api/v1/admin/settings', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getExceptions(date: string, crewId: string): Observable<ExceptionResponse[]> {
    return this.http.get<ExceptionResponse[]>('/api/v1/admin/exceptions', { params: { date, crewId } }).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  resolveException(exceptionId: string, request: ExceptionResolveRequest): Observable<ExceptionResponse> {
    return this.http.post<ExceptionResponse>(`/api/v1/admin/exceptions/${exceptionId}/resolve`, request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getPayrollSummary(): Observable<PayrollSummaryResponse> {
    return this.http.get<PayrollSummaryResponse>('/api/v1/admin/payroll/periods/current').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getPayrollPeriod(periodId: string): Observable<PayrollPeriodResponse> {
    return this.http.get<PayrollPeriodResponse>(`/api/v1/admin/payroll/periods/${periodId}`).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  exportPayroll(periodId: string): Observable<Blob> {
    return this.http.get(`/api/v1/admin/payroll/periods/${periodId}/export`, { responseType: 'blob' }).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getAuditLogs(): Observable<AuditLogResponse[]> {
    return this.http.get<AuditLogResponse[]>('/api/v1/admin/audit').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }
}

export type { ApiError };
