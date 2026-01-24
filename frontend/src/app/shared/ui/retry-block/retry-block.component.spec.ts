import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { RetryBlockComponent } from './retry-block.component';

describe('RetryBlockComponent', () => {
  let fixture: ComponentFixture<RetryBlockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RetryBlockComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(RetryBlockComponent);
  });

  it('renders title and message', () => {
    fixture.componentRef.setInput('title', 'Load failed');
    fixture.componentRef.setInput('message', 'Try again.');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Load failed');
    expect(text).toContain('Try again.');
  });

  it('emits retry on click', () => {
    const component = fixture.componentInstance;
    fixture.detectChanges();

    spyOn(component.retry, 'emit');
    const button = fixture.debugElement.query(By.css('button'));
    button.triggerEventHandler('click');

    expect(component.retry.emit).toHaveBeenCalled();
  });
});
