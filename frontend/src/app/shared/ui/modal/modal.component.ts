import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  input,
  output,
  viewChild,
  afterNextRender,
  ElementRef,
} from '@angular/core';

type ModalMode = 'modal' | 'sheet';
type CloseReason = 'backdrop' | 'escape' | 'button';

let modalSequence = 0;

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalComponent {
  readonly open = input<boolean>(false);
  readonly mode = input<ModalMode>('modal');
  readonly title = input<string>('');
  readonly description = input<string>('');
  readonly labelledBy = input<string>('');
  readonly describedBy = input<string>('');
  readonly ariaLabel = input<string>('Dialog');
  readonly closeLabel = input<string>('Close');
  readonly closeRequested = output<CloseReason>();

  readonly dialogRef = viewChild<ElementRef<HTMLElement>>('dialog');

  private readonly dialogId = `ds-modal-${modalSequence++}`;
  readonly titleId = computed(() => this.labelledBy() || `${this.dialogId}-title`);
  readonly descriptionId = computed(() => this.describedBy() || `${this.dialogId}-desc`);

  constructor() {
    effect(() => {
      if (this.open()) {
        afterNextRender(() => this.focusFirst());
      }
    });
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.closeRequested.emit('backdrop');
    }
  }

  onCloseClick(): void {
    this.closeRequested.emit('button');
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.stopPropagation();
      this.closeRequested.emit('escape');
      return;
    }

    if (event.key !== 'Tab') {
      return;
    }

    const dialog = this.dialogRef()?.nativeElement;
    if (!dialog) {
      return;
    }

    const focusable = this.getFocusableElements(dialog);
    if (focusable.length === 0) {
      event.preventDefault();
      dialog.focus();
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    const active = document.activeElement as HTMLElement | null;

    if (event.shiftKey && active === first) {
      event.preventDefault();
      last.focus();
      return;
    }

    if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus();
    }
  }

  private focusFirst(): void {
    const dialog = this.dialogRef()?.nativeElement;
    if (!dialog) {
      return;
    }

    const focusable = this.getFocusableElements(dialog);
    if (focusable.length > 0) {
      focusable[0].focus();
      return;
    }

    dialog.focus();
  }

  private getFocusableElements(root: HTMLElement): HTMLElement[] {
    return Array.from(
      root.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      ),
    ).filter((element) => !element.hasAttribute('disabled') && element.tabIndex !== -1);
  }
}
