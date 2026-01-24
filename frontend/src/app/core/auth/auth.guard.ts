import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { ApiError } from '../../features/auth/data-access/auth.api';
import { AuthSessionService } from './auth-session.service';

export const authGuard: CanActivateFn = () => {
  const session = inject(AuthSessionService);
  const router = inject(Router);

  if (session.me()) {
    return true;
  }

  return session.refresh().pipe(
    map(() => true),
    catchError((error: ApiError) => {
      session.clearLocalSession();
      router.navigate(['/auth/login']);
      return of(false);
    })
  );
};
