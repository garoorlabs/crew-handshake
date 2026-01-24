import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { FormFieldComponent } from '../form-field/form-field.component';
import { InputComponent } from './input.component';

@Component({
  template: `
    <app-form-field label="Phone" [forId]="'demo-input'" [errorId]="'demo-error'">
      <app-input id="demo-input" [ariaDescribedBy]="'demo-error'"></app-input>
    </app-form-field>
  `,
  imports: [FormFieldComponent, InputComponent],
})
class InputHostComponent {}

describe('InputComponent', () => {
  let fixture: ComponentFixture<InputHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InputHostComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InputHostComponent);
    fixture.detectChanges();
  });

  it('associates label and input via for/id', () => {
    const label = fixture.debugElement.query(By.css('label')).nativeElement as HTMLLabelElement;
    const input = fixture.debugElement.query(By.css('input')).nativeElement as HTMLInputElement;

    expect(label.getAttribute('for')).toBe('demo-input');
    expect(input.id).toBe('demo-input');
  });

  it('is focusable', () => {
    const input = fixture.debugElement.query(By.css('input')).nativeElement as HTMLInputElement;
    input.focus();

    expect(document.activeElement).toBe(input);
  });
});
