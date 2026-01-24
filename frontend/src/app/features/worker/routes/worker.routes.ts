import { Routes } from '@angular/router';
import { WorkerShellComponent } from '../../../core/ui/worker-shell/worker-shell.component';
import { WorkerCrewCallPage } from '../pages/crew-call.page';
import { WorkerTimecardPage } from '../pages/timecard.page';

export const workerRoutes: Routes = [
  {
    path: '',
    component: WorkerShellComponent,
    children: [
      { path: '', component: WorkerCrewCallPage },
      { path: 't/:token', component: WorkerCrewCallPage },
      { path: 'timecard/:token', component: WorkerTimecardPage },
    ],
  },
];
