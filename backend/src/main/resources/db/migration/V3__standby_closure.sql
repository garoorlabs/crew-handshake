ALTER TABLE crew_call_recipients
  ADD COLUMN standby_closed_at timestamp;

ALTER TABLE crew_call_recipients
  ADD COLUMN standby_send_status varchar(32);

ALTER TABLE crew_call_recipients
  ADD COLUMN standby_send_error varchar(400);
