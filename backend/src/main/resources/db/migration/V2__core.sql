ALTER TABLE companies
  ADD COLUMN default_language VARCHAR(8) NOT NULL DEFAULT 'en';

ALTER TABLE companies
  ADD COLUMN payroll_frequency VARCHAR(16) NOT NULL DEFAULT 'WEEKLY';

ALTER TABLE companies
  ADD COLUMN payroll_cutoff_day VARCHAR(16) NOT NULL DEFAULT 'FRIDAY';

ALTER TABLE companies
  ADD COLUMN standby_cutoff_time TIME NOT NULL DEFAULT '18:00:00';

ALTER TABLE companies
  ADD COLUMN dispatch_authority VARCHAR(16) NOT NULL DEFAULT 'HYBRID';

ALTER TABLE memberships
  ADD CONSTRAINT uk_memberships_company_identity UNIQUE (company_id, identity_id);

CREATE TABLE worker_profiles (
  membership_id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  display_name VARCHAR(200) NOT NULL,
  preferred_language VARCHAR(8) NOT NULL,
  crew_id UUID,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_worker_profile_membership FOREIGN KEY (membership_id) REFERENCES memberships(id),
  CONSTRAINT fk_worker_profile_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE foreman_profiles (
  membership_id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  display_name VARCHAR(200) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_foreman_profile_membership FOREIGN KEY (membership_id) REFERENCES memberships(id),
  CONSTRAINT fk_foreman_profile_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE crews (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  name VARCHAR(200) NOT NULL,
  foreman_membership_id UUID NOT NULL,
  CONSTRAINT fk_crews_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_crews_foreman FOREIGN KEY (foreman_membership_id) REFERENCES memberships(id)
);

CREATE TABLE sites (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  name VARCHAR(200) NOT NULL,
  address VARCHAR(400),
  notes VARCHAR(500),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_sites_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

ALTER TABLE worker_profiles
  ADD CONSTRAINT fk_worker_profile_crew FOREIGN KEY (crew_id) REFERENCES crews(id);

CREATE TABLE crew_calls (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  crew_id UUID NOT NULL,
  site_id UUID NOT NULL,
  start_at TIMESTAMP NOT NULL,
  work_date DATE NOT NULL,
  meet_point VARCHAR(200) NOT NULL,
  sent_by_membership_id UUID NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_crew_calls_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_crew_calls_crew FOREIGN KEY (crew_id) REFERENCES crews(id),
  CONSTRAINT fk_crew_calls_site FOREIGN KEY (site_id) REFERENCES sites(id),
  CONSTRAINT fk_crew_calls_sender FOREIGN KEY (sent_by_membership_id) REFERENCES memberships(id)
);

CREATE TABLE crew_call_recipients (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  crew_call_id UUID NOT NULL,
  worker_membership_id UUID NOT NULL,
  token_hash VARCHAR(200) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  handshake_status VARCHAR(32),
  handshake_at TIMESTAMP,
  late_eta_minutes INTEGER,
  availability_after VARCHAR(32),
  availability_different_site_ok BOOLEAN,
  availability_note VARCHAR(400),
  override_site_id UUID,
  override_start_at TIMESTAMP,
  override_meet_point VARCHAR(200),
  send_status VARCHAR(32) NOT NULL,
  send_error VARCHAR(400),
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_recipients_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_recipients_call FOREIGN KEY (crew_call_id) REFERENCES crew_calls(id),
  CONSTRAINT fk_recipients_worker FOREIGN KEY (worker_membership_id) REFERENCES memberships(id),
  CONSTRAINT fk_recipients_site_override FOREIGN KEY (override_site_id) REFERENCES sites(id),
  CONSTRAINT uk_recipients_call_worker UNIQUE (crew_call_id, worker_membership_id)
);

CREATE TABLE worker_timecard_links (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  worker_membership_id UUID NOT NULL,
  token_hash VARCHAR(200) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_timecard_links_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_timecard_links_worker FOREIGN KEY (worker_membership_id) REFERENCES memberships(id),
  CONSTRAINT uk_timecard_links_worker UNIQUE (worker_membership_id)
);

CREATE TABLE time_entries (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  worker_membership_id UUID NOT NULL,
  crew_call_id UUID NOT NULL,
  work_date DATE NOT NULL,
  check_in_at TIMESTAMP,
  check_out_at TIMESTAMP,
  source VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  edited BOOLEAN NOT NULL DEFAULT FALSE,
  edit_reason VARCHAR(200),
  edit_note VARCHAR(400),
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_time_entries_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_time_entries_worker FOREIGN KEY (worker_membership_id) REFERENCES memberships(id),
  CONSTRAINT fk_time_entries_call FOREIGN KEY (crew_call_id) REFERENCES crew_calls(id),
  CONSTRAINT uk_time_entries_unique UNIQUE (worker_membership_id, crew_call_id, work_date)
);

CREATE TABLE review_requests (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  worker_membership_id UUID NOT NULL,
  work_date DATE NOT NULL,
  reason VARCHAR(120) NOT NULL,
  note VARCHAR(400),
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_review_requests_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_review_requests_worker FOREIGN KEY (worker_membership_id) REFERENCES memberships(id)
);

CREATE TABLE exceptions (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  crew_id UUID NOT NULL,
  worker_membership_id UUID NOT NULL,
  type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  time_entry_id UUID,
  review_request_id UUID,
  resolution_action VARCHAR(32),
  resolution_reason VARCHAR(200),
  resolution_note VARCHAR(400),
  resolved_by_membership_id UUID,
  resolved_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_exceptions_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_exceptions_crew FOREIGN KEY (crew_id) REFERENCES crews(id),
  CONSTRAINT fk_exceptions_worker FOREIGN KEY (worker_membership_id) REFERENCES memberships(id),
  CONSTRAINT fk_exceptions_time_entry FOREIGN KEY (time_entry_id) REFERENCES time_entries(id),
  CONSTRAINT fk_exceptions_review_request FOREIGN KEY (review_request_id) REFERENCES review_requests(id),
  CONSTRAINT fk_exceptions_resolved_by FOREIGN KEY (resolved_by_membership_id) REFERENCES memberships(id)
);

CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  actor_membership_id UUID NOT NULL,
  action_type VARCHAR(64) NOT NULL,
  entity_type VARCHAR(64) NOT NULL,
  entity_id UUID NOT NULL,
  details_json TEXT,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_audit_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_membership_id) REFERENCES memberships(id)
);
