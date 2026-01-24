import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FieldErrorComponent } from './field-error.component';

describe('FieldErrorComponent', () => {
  let fixture: ComponentFixture<FieldErrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FieldErrorComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FieldErrorComponent);
  });

  it('renders the error message', () => {
    fixture.componentRef.setInput('message', 'Required');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Required');
  });

  it('sets the host id when provided', () => {
    fixture.componentRef.setInput('id', 'name-error');
    fixture.componentRef.setInput('message', 'Required');
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.id).toBe('name-error');
  });
});
