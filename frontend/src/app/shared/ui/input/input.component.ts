import {
  ChangeDetectionStrategy,
  Component,
  computed,
  forwardRef,
  input,
  signal,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

type InputType = 'text' | 'email' | 'tel' | 'password' | 'number' | 'date' | 'datetime-local';

@Component({
  selector: 'app-input',
  templateUrl: './input.component.html',
  styleUrl: './input.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    },
  ],
})
export class InputComponent implements ControlValueAccessor {
  readonly id = input<string>('');
  readonly name = input<string>('');
  readonly type = input<InputType>('text');
  readonly placeholder = input<string>('');
  readonly autocomplete = input<string>('');
  readonly inputMode = input<string>('');
  readonly disabled = input<boolean>(false);
  readonly invalid = input<boolean>(false);
  readonly ariaDescribedBy = input<string>('');

  private readonly disabledState = signal(false);
  private readonly valueState = signal('');
  readonly value = computed(() => this.valueState());
  readonly isDisabled = computed(() => this.disabled() || this.disabledState());

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  writeValue(value: string | null): void {
    this.valueState.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabledState.set(isDisabled);
  }

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    const nextValue = target?.value ?? '';
    this.valueState.set(nextValue);
    this.onChange(nextValue);
  }

  onBlur(): void {
    this.onTouched();
  }
}
