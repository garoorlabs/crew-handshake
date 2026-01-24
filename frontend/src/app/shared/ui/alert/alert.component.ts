import { ChangeDetectionStrategy, Component, input } from '@angular/core';

type AlertTone = 'info' | 'success' | 'warning' | 'error';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ds-alert',
    '[class.ds-alert--success]': 'tone() === "success"',
    '[class.ds-alert--warning]': 'tone() === "warning"',
    '[class.ds-alert--error]': 'tone() === "error"',
  },
})
export class AlertComponent {
  readonly tone = input<AlertTone>('info');
}
