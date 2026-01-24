import { HttpErrorResponse } from '@angular/common/http';
import { mapHttpError } from './api-error';

describe('mapHttpError', () => {
  it('maps known error codes', () => {
    const error = new HttpErrorResponse({
      status: 403,
      error: { errorCode: 'FORBIDDEN', message: 'Not permitted' },
    });

    const mapped = mapHttpError(error);

    expect(mapped.category).toBe('Forbidden');
    expect(mapped.message).toBe('Not permitted');
  });

  it('falls back to Unknown', () => {
    const error = new HttpErrorResponse({
      status: 500,
      error: { errorCode: 'NOT_A_CODE', message: 'Oops' },
    });

    const mapped = mapHttpError(error);

    expect(mapped.category).toBe('Unknown');
    expect(mapped.message).toBe('Oops');
  });

  it('preserves field errors', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        errorCode: 'VALIDATION_ERROR',
        message: 'Validation failed',
        fieldErrors: { name: 'Required' },
      },
    });

    const mapped = mapHttpError(error);

    expect(mapped.category).toBe('Validation');
    expect(mapped.fieldErrors?.['name']).toBe('Required');
  });
});
