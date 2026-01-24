import { Routes } from '@angular/router';
import { activeCompanyGuard } from './core/auth/active-company.guard';
import { authGuard } from './core/auth/auth.guard';
import { NotFoundPage } from './core/ui/not-found/not-found.page';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'a'
  },
  {
    path: 'a',
    canActivate: [authGuard, activeCompanyGuard],
    loadChildren: () => import('./features/admin/routes/admin.routes').then((m) => m.adminRoutes)
  },
  {
    path: 'f',
    canActivate: [authGuard, activeCompanyGuard],
    loadChildren: () => import('./features/foreman/routes/foreman.routes').then((m) => m.foremanRoutes)
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/routes/auth.routes').then((m) => m.authRoutes)
  },
  {
    path: 'w',
    loadChildren: () => import('./features/worker/routes/worker.routes').then((m) => m.workerRoutes)
  },
  {
    path: '**',
    component: NotFoundPage
  }
];
