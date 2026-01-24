import { HttpErrorResponse } from '@angular/common/http';

export type ApiErrorCategory =
  | 'Unauthorized'
  | 'Forbidden'
  | 'NotFound'
  | 'Validation'
  | 'RateLimited'
  | 'Unknown';

export interface ApiError {
  category: ApiErrorCategory;
  message: string;
  fieldErrors?: Record<string, string>;
}

interface ApiErrorResponse {
  errorCode?: string;
  message?: string;
  fieldErrors?: Record<string, string>;
}

const errorCodeMap: Record<string, ApiErrorCategory> = {
  UNAUTHORIZED: 'Unauthorized',
  FORBIDDEN: 'Forbidden',
  NOT_FOUND: 'NotFound',
  VALIDATION_ERROR: 'Validation',
  RATE_LIMITED: 'RateLimited',
};

export function mapHttpError(error: HttpErrorResponse): ApiError {
  const payload = error.error as ApiErrorResponse | null;
  const category = payload?.errorCode ? (errorCodeMap[payload.errorCode] ?? 'Unknown') : 'Unknown';
  return {
    category,
    message: payload?.message ?? 'Something went wrong',
    fieldErrors: payload?.fieldErrors,
  };
}
