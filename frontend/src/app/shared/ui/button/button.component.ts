import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrl: './button.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ButtonComponent {
  readonly variant = input<'primary' | 'secondary' | 'ghost' | 'destructive'>('primary');
  readonly type = input<'button' | 'submit' | 'reset'>('button');
  readonly disabled = input(false);
  readonly fullWidth = input(false);
  readonly ariaLabel = input<string | null>(null);
  readonly clicked = output<Event>();

  onClick(event: Event): void {
    this.clicked.emit(event);
  }
}
