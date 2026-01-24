import { Routes } from '@angular/router';
import { AuthShellPage } from '../pages/auth-shell.page';
import { AuthCompanySelectPage } from '../pages/company-select.page';
import { AuthLoginPage } from '../pages/login.page';

export const authRoutes: Routes = [
  {
    path: '',
    component: AuthShellPage,
    children: [
      { path: 'login', component: AuthLoginPage },
      { path: 'company', component: AuthCompanySelectPage },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  }
];
