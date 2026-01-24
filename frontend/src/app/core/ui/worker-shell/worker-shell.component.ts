import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-worker-shell',
  imports: [RouterOutlet],
  templateUrl: './worker-shell.component.html',
  styleUrl: './worker-shell.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ds-layout-worker'
  }
})
export class WorkerShellComponent {}
