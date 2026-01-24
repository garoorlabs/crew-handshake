import { Routes } from '@angular/router';
import { ForemanShellComponent } from '../../../core/ui/foreman-shell/foreman-shell.component';
import { ForemanCrewCallsPage } from '../pages/crew-calls.page';
import { ForemanExceptionsPage } from '../pages/exceptions.page';
import { ForemanRollCallPage } from '../pages/roll-call.page';
import { ForemanTimeAdjustmentsPage } from '../pages/time-adjustments.page';
import { ForemanTodayPage } from '../pages/today.page';

export const foremanRoutes: Routes = [
  {
    path: '',
    component: ForemanShellComponent,
    children: [
      { path: '', redirectTo: 'today', pathMatch: 'full' },
      { path: 'today', component: ForemanTodayPage },
      { path: 'crew-calls', component: ForemanCrewCallsPage },
      { path: 'roll-call', component: ForemanRollCallPage },
      { path: 'exceptions', component: ForemanExceptionsPage },
      { path: 'time-adjustments', component: ForemanTimeAdjustmentsPage },
    ],
  },
];
