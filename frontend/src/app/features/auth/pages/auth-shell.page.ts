import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-shell-page',
  imports: [RouterOutlet],
  templateUrl: './auth-shell.page.html',
  styleUrl: './auth-shell.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ds-layout-foreman',
  },
})
export class AuthShellPage {}
