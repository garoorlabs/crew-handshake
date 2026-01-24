import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-field-error',
  templateUrl: './field-error.component.html',
  styleUrl: './field-error.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'ds-error-text',
    role: 'status',
    '[attr.id]': 'id()'
  }
})
export class FieldErrorComponent {
  readonly id = input<string | null>(null);
  readonly message = input<string>('');
}
