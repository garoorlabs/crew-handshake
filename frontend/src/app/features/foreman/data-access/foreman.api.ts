import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { ApiError, mapHttpError } from '../../../core/http/api-error';

export interface ForemanCrewSummary {
  crewId: string;
  name: string;
}

export interface SiteSummary {
  siteId: string;
  name: string;
  address: string | null;
}

export interface CrewCallSummaryResponse {
  crewCallId: string;
  siteName: string;
  startAt: string;
  meetPoint: string;
  sentByName: string;
  status: string;
}

export interface CrewCallCreateRequest {
  crewId: string;
  siteId: string;
  startAt: string;
  meetPoint: string;
}

export interface CrewCallUpdateRequest {
  siteId: string;
  startAt: string;
  meetPoint: string;
}

export interface CrewCallRecipientStatus {
  workerMembershipId: string;
  workerName: string;
  phoneE164: string;
  sendStatus: string;
  sendError: string | null;
}

export interface CrewCallResponse {
  crewCallId: string;
  status: string;
  recipients: CrewCallRecipientStatus[];
}

export interface TodayWorkerStatus {
  membershipId: string;
  displayName: string;
  phoneE164: string;
  handshakeStatus: string | null;
  lateEtaMinutes: number | null;
  checkInAt: string | null;
  checkOutAt: string | null;
  hasException: boolean;
  timeEntryId: string | null;
}

export interface TodayBoardResponse {
  crewId: string;
  crewName: string;
  date: string;
  crewCallId: string | null;
  siteName: string | null;
  startAt: string | null;
  meetPoint: string | null;
  workers: TodayWorkerStatus[];
}

export type RollCallStatus = 'PRESENT' | 'LATE' | 'ABSENT';

export interface RollCallEntry {
  workerMembershipId: string;
  status: RollCallStatus;
}

export interface RollCallRequest {
  crewId: string;
  date: string;
  recordedAt?: string | null;
  entries: RollCallEntry[];
}

export interface RollCallResponse {
  crewId: string;
  date: string;
  updatedCount: number;
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

export interface TimeAdjustmentRequest {
  timeEntryId: string;
  checkInAt: string;
  checkOutAt: string;
  reason: string;
  note?: string | null;
}

export interface TimeAdjustmentResponse {
  timeEntryId: string;
  checkInAt: string;
  checkOutAt: string;
  status: string;
  edited: boolean;
}

export interface RecipientOverrideRequest {
  crewCallId: string;
  workerMembershipId: string;
  siteId: string;
  startAt: string;
  meetPoint: string;
}

@Injectable({ providedIn: 'root' })
export class ForemanApi {
  private readonly http = inject(HttpClient);

  getCrews(): Observable<ForemanCrewSummary[]> {
    return this.http.get<ForemanCrewSummary[]>('/api/v1/foreman/crews').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getSites(): Observable<SiteSummary[]> {
    return this.http.get<SiteSummary[]>('/api/v1/foreman/sites').pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getCrewCalls(date: string, crewId: string): Observable<CrewCallSummaryResponse[]> {
    return this.http.get<CrewCallSummaryResponse[]>('/api/v1/foreman/crew-calls', { params: { date, crewId } }).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  createCrewCall(request: CrewCallCreateRequest): Observable<CrewCallResponse> {
    return this.http.post<CrewCallResponse>('/api/v1/foreman/crew-calls', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  resendCrewCall(crewCallId: string, request: CrewCallUpdateRequest): Observable<CrewCallResponse> {
    return this.http.post<CrewCallResponse>(`/api/v1/foreman/crew-calls/${crewCallId}/resend`, request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getToday(date: string, crewId: string): Observable<TodayBoardResponse> {
    return this.http.get<TodayBoardResponse>('/api/v1/foreman/today', { params: { date, crewId } }).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  submitRollCall(request: RollCallRequest): Observable<RollCallResponse> {
    return this.http.post<RollCallResponse>('/api/v1/foreman/roll-call', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getExceptions(date: string, crewId: string): Observable<ExceptionResponse[]> {
    return this.http.get<ExceptionResponse[]>('/api/v1/foreman/exceptions', { params: { date, crewId } }).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  resolveException(exceptionId: string, request: ExceptionResolveRequest): Observable<ExceptionResponse> {
    return this.http.post<ExceptionResponse>(`/api/v1/foreman/exceptions/${exceptionId}/resolve`, request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  adjustTime(request: TimeAdjustmentRequest): Observable<TimeAdjustmentResponse> {
    return this.http.post<TimeAdjustmentResponse>('/api/v1/foreman/time-adjustments', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  sendRecipientOverride(request: RecipientOverrideRequest): Observable<CrewCallRecipientStatus> {
    return this.http.post<CrewCallRecipientStatus>('/api/v1/foreman/recipient-overrides', request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }
}

export type { ApiError };
