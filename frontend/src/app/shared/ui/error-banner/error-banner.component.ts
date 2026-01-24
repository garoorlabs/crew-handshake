import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-error-banner',
  templateUrl: './error-banner.component.html',
  styleUrl: './error-banner.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErrorBannerComponent {
  readonly message = input<string>('Something went wrong.');
  readonly showRetry = input<boolean>(false);
  readonly retryLabel = input<string>('Retry');
  readonly retry = output<void>();
}
