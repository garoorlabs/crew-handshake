import { computed, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  GuardResult,
  MaybeAsync,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { firstValueFrom, isObservable, of, throwError } from 'rxjs';
import { MeResponse } from '../../features/auth/data-access/auth.api';
import { AuthSessionService } from './auth-session.service';
import { authGuard } from './auth.guard';

const baseMe = (overrides: Partial<MeResponse> = {}): MeResponse => ({
  identityId: 'identity-1',
  phoneE164: '+14155550100',
  activeCompanyId: null,
  activeMembershipId: null,
  activeRoles: [],
  memberships: [],
  ...overrides,
});

const resolveGuard = async (result: MaybeAsync<GuardResult>) => {
  if (isObservable(result)) {
    return firstValueFrom(result);
  }
  if (result instanceof Promise) {
    return result;
  }
  return result;
};

const dummyRoute = {} as ActivatedRouteSnapshot;
const dummyState = { url: '/a' } as RouterStateSnapshot;

const createSessionStub = (initial: MeResponse | null = null) => {
  const meSignal = signal<MeResponse | null>(initial);
  const sessionStub = {
    me: meSignal,
    hasActiveCompany: computed(() => !!meSignal()?.activeCompanyId),
    refresh: jasmine.createSpy('refresh'),
    clearLocalSession: jasmine.createSpy('clearLocalSession'),
    setMe: (value: MeResponse | null) => meSignal.set(value),
  };
  return sessionStub;
};

describe('authGuard', () => {
  it('allows when session already loaded', async () => {
    const session = createSessionStub(baseMe({ activeCompanyId: 'company-1' }));
    const router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthSessionService, useValue: session },
        { provide: Router, useValue: router },
      ],
    });

    const result = await resolveGuard(
      TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeTrue();
    expect(session.refresh).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('refreshes session and allows on success', async () => {
    const session = createSessionStub();
    const router = jasmine.createSpyObj('Router', ['navigate']);
    const me = baseMe({ activeCompanyId: 'company-1' });

    session.refresh.and.callFake(() => {
      session.setMe(me);
      return of(me);
    });

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthSessionService, useValue: session },
        { provide: Router, useValue: router },
      ],
    });

    const result = await resolveGuard(
      TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeTrue();
    expect(session.refresh).toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects to login on refresh error', async () => {
    const session = createSessionStub();
    const router = jasmine.createSpyObj('Router', ['navigate']);

    session.refresh.and.returnValue(
      throwError(() => ({ category: 'Unauthorized', message: 'Unauthorized' })),
    );

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthSessionService, useValue: session },
        { provide: Router, useValue: router },
      ],
    });

    const result = await resolveGuard(
      TestBed.runInInjectionContext(() => authGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeFalse();
    expect(session.clearLocalSession).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
