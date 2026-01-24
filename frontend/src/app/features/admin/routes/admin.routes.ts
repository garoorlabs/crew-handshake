import { Routes } from '@angular/router';
import { AdminShellComponent } from '../../../core/ui/admin-shell/admin-shell.component';
import { AdminAuditPage } from '../pages/audit.page';
import { AdminCrewsPage } from '../pages/crews.page';
import { AdminExceptionsPage } from '../pages/exceptions.page';
import { AdminForemenPage } from '../pages/foremen.page';
import { AdminPayrollPage } from '../pages/payroll.page';
import { AdminSettingsPage } from '../pages/settings.page';
import { AdminSitesPage } from '../pages/sites.page';
import { AdminWorkersPage } from '../pages/workers.page';

export const adminRoutes: Routes = [
  {
    path: '',
    component: AdminShellComponent,
    children: [
      { path: '', redirectTo: 'workers', pathMatch: 'full' },
      { path: 'workers', component: AdminWorkersPage },
      { path: 'foremen', component: AdminForemenPage },
      { path: 'crews', component: AdminCrewsPage },
      { path: 'sites', component: AdminSitesPage },
      { path: 'settings', component: AdminSettingsPage },
      { path: 'exceptions', component: AdminExceptionsPage },
      { path: 'payroll', component: AdminPayrollPage },
      { path: 'audit', component: AdminAuditPage }
    ]
  }
];
