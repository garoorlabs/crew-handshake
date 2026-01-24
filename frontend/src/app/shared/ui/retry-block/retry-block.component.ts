import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-retry-block',
  templateUrl: './retry-block.component.html',
  styleUrl: './retry-block.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ds-card',
  },
})
export class RetryBlockComponent {
  readonly title = input<string>('Something went wrong');
  readonly message = input<string>('Please try again.');
  readonly actionLabel = input<string>('Retry');
  readonly retry = output<void>();
}
