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
import { activeCompanyGuard } from './active-company.guard';
import { AuthSessionService } from './auth-session.service';

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

describe('activeCompanyGuard', () => {
  it('allows when active company is present in session', async () => {
    const session = createSessionStub(baseMe({ activeCompanyId: 'company-1' }));
    const router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthSessionService, useValue: session },
        { provide: Router, useValue: router },
      ],
    });

    const result = await resolveGuard(
      TestBed.runInInjectionContext(() => activeCompanyGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects to company selection when active company is missing', async () => {
    const session = createSessionStub(baseMe());
    const router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthSessionService, useValue: session },
        { provide: Router, useValue: router },
      ],
    });

    const result = await resolveGuard(
      TestBed.runInInjectionContext(() => activeCompanyGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/company']);
  });

  it('refreshes session and allows when active company is set', async () => {
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
      TestBed.runInInjectionContext(() => activeCompanyGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeTrue();
    expect(session.refresh).toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('refreshes session and redirects when active company is still missing', async () => {
    const session = createSessionStub();
    const router = jasmine.createSpyObj('Router', ['navigate']);
    const me = baseMe();

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
      TestBed.runInInjectionContext(() => activeCompanyGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/company']);
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
      TestBed.runInInjectionContext(() => activeCompanyGuard(dummyRoute, dummyState)),
    );

    expect(result).toBeFalse();
    expect(session.clearLocalSession).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
