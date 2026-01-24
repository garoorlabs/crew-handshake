import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-form-field',
  templateUrl: './form-field.component.html',
  styleUrl: './form-field.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormFieldComponent {
  readonly label = input<string>('');
  readonly hint = input<string>('');
  readonly error = input<string>('');
  readonly forId = input<string>('');
  readonly errorId = input<string>('');
}
