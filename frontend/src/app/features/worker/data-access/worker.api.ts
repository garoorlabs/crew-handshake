import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { ApiError, mapHttpError } from '../../../core/http/api-error';

export type WorkerAction =
  | 'CONFIRM'
  | 'LATE'
  | 'CANT'
  | 'NEED_CHANGE'
  | 'CHECK_IN'
  | 'CHECK_OUT'
  | 'VIEW_RECEIPT'
  | 'NONE';

export interface WorkerCrewCallResponse {
  crewCallId: string;
  companyName: string;
  crewName: string;
  siteName: string;
  siteAddress: string | null;
  startAt: string;
  meetPoint: string;
  senderName: string;
  handshakeStatus: string | null;
  lateEtaMinutes: number | null;
  availabilityAfter: string | null;
  availabilityDifferentSiteOk: boolean | null;
  availabilityNote: string | null;
  checkInAt: string | null;
  checkOutAt: string | null;
  primaryAction: WorkerAction;
  availableActions: WorkerAction[];
  timecardToken: string;
  needsAvailability: boolean;
}

export interface WorkerHandshakeRequest {
  status: 'CONFIRMED' | 'LATE' | 'CANT' | 'NEED_CHANGE';
  lateEtaMinutes?: number | null;
}

export interface WorkerAvailabilityRequest {
  availabilityAfter: 'AFTER_9' | 'AFTER_10' | 'AFTER_12' | 'NOT_TODAY';
  differentSiteOk?: boolean | null;
  note?: string | null;
}

export interface WorkerCheckInResponse {
  checkInAt: string;
}

export interface WorkerCheckOutResponse {
  checkOutAt: string;
}

export interface WorkerTimecardEntry {
  timeEntryId: string;
  workDate: string;
  crewName: string;
  siteName: string;
  checkInAt: string | null;
  checkOutAt: string | null;
  status: string;
  edited: boolean;
  editReason: string | null;
  reviewStatus: string;
}

export interface WorkerTimecardResponse {
  weekStart: string;
  weekEnd: string;
  entries: WorkerTimecardEntry[];
}

export interface WorkerReviewRequestCreateRequest {
  workDate: string;
  reason: string;
  note?: string | null;
}

export interface WorkerReviewRequestResponse {
  reviewRequestId: string;
  status: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class WorkerApi {
  private readonly http = inject(HttpClient);

  getCrewCall(token: string): Observable<WorkerCrewCallResponse> {
    return this.http.get<WorkerCrewCallResponse>(`/api/v1/public/worker/crew-calls/by-link/${token}`).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  submitHandshake(token: string, request: WorkerHandshakeRequest): Observable<WorkerCrewCallResponse> {
    return this.http.post<WorkerCrewCallResponse>(`/api/v1/public/worker/crew-calls/by-link/${token}/handshake`, request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  submitAvailability(token: string, request: WorkerAvailabilityRequest): Observable<WorkerCrewCallResponse> {
    return this.http.post<WorkerCrewCallResponse>(`/api/v1/public/worker/crew-calls/by-link/${token}/availability`, request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  checkIn(token: string): Observable<WorkerCheckInResponse> {
    return this.http.post<WorkerCheckInResponse>(`/api/v1/public/worker/crew-calls/by-link/${token}/check-in`, {}).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  checkOut(token: string): Observable<WorkerCheckOutResponse> {
    return this.http.post<WorkerCheckOutResponse>(`/api/v1/public/worker/crew-calls/by-link/${token}/check-out`, {}).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  getTimecard(token: string, week?: string): Observable<WorkerTimecardResponse> {
    return this.http.get<WorkerTimecardResponse>(`/api/v1/public/worker/timecard/by-link/${token}`, {
      params: week ? { week } : {}
    }).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }

  submitReviewRequest(token: string, request: WorkerReviewRequestCreateRequest): Observable<WorkerReviewRequestResponse> {
    return this.http.post<WorkerReviewRequestResponse>(`/api/v1/public/worker/timecard/by-link/${token}/review-requests`, request).pipe(
      catchError((error) => throwError(() => mapHttpError(error)))
    );
  }
}

export type { ApiError };
