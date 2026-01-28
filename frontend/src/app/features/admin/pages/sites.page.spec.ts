import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { AdminSitesPage } from './sites.page';
import { AdminApi, ApiError } from '../data-access/admin.api';

describe('AdminSitesPage', () => {
  let fixture: ComponentFixture<AdminSitesPage>;
  let adminApi: jasmine.SpyObj<AdminApi>;

  beforeEach(async () => {
    adminApi = jasmine.createSpyObj<AdminApi>('AdminApi', ['getSites', 'createSite', 'updateSite']);
    adminApi.getSites.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AdminSitesPage],
      providers: [{ provide: AdminApi, useValue: adminApi }],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminSitesPage);
    fixture.detectChanges();
  });

  it('maps validation errors to the name field', () => {
    const validationError: ApiError = {
      category: 'Validation',
      message: 'Validation failed',
      fieldErrors: { name: 'Name is required' },
    };
    adminApi.createSite.and.returnValue(throwError(() => validationError));

    fixture.componentInstance.form.controls.name.setValue('Central Yard');
    fixture.componentInstance.form.controls.address.setValue('');
    fixture.componentInstance.form.controls.notes.setValue('');

    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(By.css('#site-name-error'));
    expect(errorElement).not.toBeNull();
    expect(errorElement.nativeElement.textContent).toContain('Name is required');
    expect(fixture.componentInstance.form.controls.name.errors?.['server']).toBe(
      'Name is required',
    );
  });
});
