import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { AdminForemenPage } from './foremen.page';
import { AdminApi, ApiError } from '../data-access/admin.api';

describe('AdminForemenPage', () => {
  let fixture: ComponentFixture<AdminForemenPage>;
  let adminApi: jasmine.SpyObj<AdminApi>;

  beforeEach(async () => {
    adminApi = jasmine.createSpyObj<AdminApi>('AdminApi', [
      'getForemen',
      'createForeman',
      'updateForeman',
    ]);
    adminApi.getForemen.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AdminForemenPage],
      providers: [{ provide: AdminApi, useValue: adminApi }],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminForemenPage);
    fixture.detectChanges();
  });

  it('maps validation errors to the name field', () => {
    const validationError: ApiError = {
      category: 'Validation',
      message: 'Validation failed',
      fieldErrors: { displayName: 'Name is required' },
    };
    adminApi.createForeman.and.returnValue(throwError(() => validationError));

    fixture.componentInstance.form.controls.displayName.setValue('Foreman');
    fixture.componentInstance.form.controls.phone.setValue('+14155550000');

    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(By.css('#foreman-name-error'));
    expect(errorElement).not.toBeNull();
    expect(errorElement.nativeElement.textContent).toContain('Name is required');
    expect(fixture.componentInstance.form.controls.displayName.errors?.['server']).toBe(
      'Name is required',
    );
  });
});
