import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { AdminWorkersPage } from './workers.page';
import { AdminApi, ApiError } from '../data-access/admin.api';

describe('AdminWorkersPage', () => {
  let fixture: ComponentFixture<AdminWorkersPage>;
  let adminApi: jasmine.SpyObj<AdminApi>;

  beforeEach(async () => {
    adminApi = jasmine.createSpyObj<AdminApi>('AdminApi', [
      'getWorkers',
      'getCrews',
      'createWorker',
      'updateWorker',
    ]);
    adminApi.getWorkers.and.returnValue(of([]));
    adminApi.getCrews.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [AdminWorkersPage],
      providers: [{ provide: AdminApi, useValue: adminApi }],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminWorkersPage);
    fixture.detectChanges();
  });

  it('maps validation errors to the crew field', () => {
    const validationError: ApiError = {
      category: 'Validation',
      message: 'Validation failed',
      fieldErrors: { crewId: 'Worker must be active to assign to a crew' },
    };
    adminApi.createWorker.and.returnValue(throwError(() => validationError));

    fixture.componentInstance.form.controls.displayName.setValue('Worker');
    fixture.componentInstance.form.controls.phone.setValue('+14155550000');
    fixture.componentInstance.form.controls.crewId.setValue('crew-1');

    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(By.css('#worker-crew-error'));
    expect(errorElement).not.toBeNull();
    expect(errorElement.nativeElement.textContent).toContain(
      'Worker must be active to assign to a crew',
    );
    expect(fixture.componentInstance.form.controls.crewId.errors?.['server']).toBe(
      'Worker must be active to assign to a crew',
    );
  });
});
