CREATE TABLE companies (
  id UUID PRIMARY KEY,
  name VARCHAR(200) NOT NULL
);

CREATE TABLE identities (
  id UUID PRIMARY KEY,
  phone_e164 VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE memberships (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  identity_id UUID NOT NULL,
  CONSTRAINT fk_memberships_company FOREIGN KEY (company_id) REFERENCES companies(id),
  CONSTRAINT fk_memberships_identity FOREIGN KEY (identity_id) REFERENCES identities(id)
);

CREATE TABLE membership_roles (
  membership_id UUID NOT NULL,
  role VARCHAR(32) NOT NULL,
  CONSTRAINT fk_membership_roles_membership FOREIGN KEY (membership_id) REFERENCES memberships(id)
);

CREATE TABLE otp_codes (
  id UUID PRIMARY KEY,
  identity_id UUID NOT NULL,
  code_hash VARCHAR(200) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  attempts INTEGER NOT NULL,
  max_attempts INTEGER NOT NULL,
  consumed_at TIMESTAMP,
  CONSTRAINT fk_otp_codes_identity FOREIGN KEY (identity_id) REFERENCES identities(id)
);
