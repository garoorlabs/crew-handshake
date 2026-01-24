import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ErrorBannerComponent } from './error-banner.component';

describe('ErrorBannerComponent', () => {
  let fixture: ComponentFixture<ErrorBannerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorBannerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorBannerComponent);
  });

  it('renders the message', () => {
    fixture.componentRef.setInput('message', 'Network error');
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Network error');
  });

  it('emits retry when action is clicked', () => {
    const component = fixture.componentInstance;
    fixture.componentRef.setInput('showRetry', true);
    fixture.detectChanges();

    spyOn(component.retry, 'emit');
    const button = fixture.debugElement.query(By.css('button'));
    button.triggerEventHandler('click');

    expect(component.retry.emit).toHaveBeenCalled();
  });
});
