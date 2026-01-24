CREATE TABLE otp_request_attempts (
  id UUID PRIMARY KEY,
  phone_e164 VARCHAR(32) NOT NULL,
  ip_address VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_otp_request_attempts_phone_created
  ON otp_request_attempts (phone_e164, created_at);

CREATE INDEX idx_otp_request_attempts_ip_created
  ON otp_request_attempts (ip_address, created_at);
