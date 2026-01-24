# PRD -- Crew Handshake (MVP v1)
## Product Context
- Product name: Crew Handshake
- Product type: B2B SaaS (multi-tenant)
- Primary user roles: Admin, Foreman, Worker (W2/1099)
- Core problem being solved: construction crews miss starts and payroll gets disputed because dispatch and time records are fragmented, unreliable, and hard to reconcile
- Why existing solutions fail:
  - They assume stable schedules and predictable sites; construction is neither.
  - They require app installs, logins, training, and daily compliance -- unrealistic for transient labor.
  - They are built around timesheets or project management, not the daily coordination loop.
  - They feel like surveillance (GPS, productivity scoring), which reduces adoption and trust.
  - They produce data but do not reduce conflict; they often document disputes instead of preventing them.

## Non-Negotiable Constraints (must be satisfied)
- Monorepo: Angular v20+ frontend + Spring Boot REST backend
- Split deploy: frontend static + backend API
- API style: REST only (/api/v1)
- Frontend: UI never calls HTTP directly (FeatureApi only)
- Accessibility: WCAG AA minimum
- Design system: token-based, systemized, no ad-hoc UI

---

## 1) Problem Definition

### Real user pain (functional)
Small construction businesses repeatedly fail the same operational loop:
- Workers do not reliably know where to be and when (site/time/meet point changes).
- Foremen and office staff spend time chasing confirmations via calls/texts.
- Time records are incomplete or inconsistent (missed punches, paper, memory-based reconciliation).
- Payroll export becomes a weekly scramble with corrections and disputes.

### Real user pain (emotional/trust)
- Workers fear tracking and feel exposed; they care most about pay accuracy.
- Foremen feel blamed for attendance/time issues but lack simple tools that fit field reality.
- Owners/admins feel stuck between we need accuracy and my crews will not use software.

### Who experiences it, and when
- Daily (evening before / morning of): unclear instructions -> lateness/no-shows -> job delays.
- Daily (end of shift): missing check-outs -> confusion about hours.
- Weekly (payroll cutoff): disputed hours -> admin time, worker frustration, delayed payroll.

### Why it matters now
- Labor markets are tighter; retention depends on predictable pay and fewer disputes.
- More jobsite volatility (multi-site days, last-minute changes) increases coordination load.
- Small contractors are under margin pressure; unplanned admin time and lost hours matter.

---

## 2) Goals and Non-Goals

### Goals (measurable outcomes)
1. Reduce morning coordination chaos
   - Within 2 weeks of use, 70% of workdays have a Crew Call sent for active crews.
2. Increase reliable show-up clarity
   - 60% of recipients open the Crew Call link on days they are scheduled.
   - 50% of recipients explicitly respond (Confirm / Late / Cant / Need change).
3. Capture time reliably without requiring full compliance
   - 75% of scheduled worker-days have time captured via (Check-in/out OR roll call).
4. Reduce disputes and rework in payroll
   - Median time to resolve missing punch exceptions < 48 hours.
   - Workers have a self-serve view of hours; review requests replace ad-hoc arguing.

### Non-Goals (explicit exclusions; prevents scope creep)
The MVP will not include:
- Task assignment (what to do today), productivity scoring, or crew performance ranking.
- Full scheduling/Gantt, project management workflows, work breakdown structures.
- Open shift marketplace, self-assignment, or bidding for shifts.
- Job costing, cost codes, invoicing, change order workflows.
- Payroll provider integrations (MVP exports CSV only).
- Continuous GPS tracking or all-day location monitoring.
- Complex labor compliance engines (union rules, prevailing wage calculations).
- Offline-first native app with sync engine.

Edge cases intentionally excluded from MVP behavior:
- Multi-break rules, meal penalty rules, jurisdiction-specific overtime calculations.
- Splitting a single day into multiple payable segments per jobsite/cost code.
- Biometric verification, photo verification, or device attestation.

---

## 3) User Personas and Roles

### 3.1 Admin (Owner / Payroll / PM / Dispatcher)
- Who they are: Runs payroll and operations; accountable for staffing readiness and pay accuracy.
- Responsible for:
  - Company setup and settings (payroll cutoffs, dispatch authority, language defaults).
  - Managing roster: workers, foremen, crews, and sites.
  - Payroll readiness review and export.
  - Audit visibility and governance.
- Explicitly not allowed to do (by design):
  - Create worker check-ins/outs as the worker (no impersonation via worker token flows).
  - Bypass audit trail for time edits (all edits require reason and are logged).

### 3.2 Foreman (Crew Lead)
- Who they are: Runs daily field operations; primary driver of morning reliability.
- Responsible for:
  - Sending Crew Calls when permitted by company dispatch authority.
  - Monitoring confirmations/no-shows for their crew.
  - Roll call when worker link usage fails.
  - Resolving exceptions quickly with minimal admin work.
- Explicitly not allowed to do (by design):
  - Modify crew roster (add/remove workers from crews).
  - Move workers across crews (cross-crew allocation).
  - Change company payroll settings.
  - Make time edits without reason/audit trail.

### 3.3 Worker (W2/1099)
- Who they are: Receives instructions, performs work, expects correct pay.
- Responsible for:
  - Acknowledging Crew Calls (Confirm/Late/Cant/Need change).
  - Checking in/out when able.
  - Reviewing their hours and submitting structured review requests when incorrect.
- Explicitly not allowed to do (by design):
  - View other workers data.
  - Access admin/foreman portals.
  - Modify recorded time directly (they request review, they do not edit).

---

## 4) Core User Journeys (Step-by-Step)

### 4.1 Company onboarding (Admin)
1) Admin logs in via OTP -> system verifies and creates session.
   - Error: invalid OTP -> system returns UNAUTHORIZED with safe message.
2) Admin selects active company (if multiple memberships) -> system sets activeCompanyId.
3) Admin creates/edits Sites -> system validates required fields and saves.
   - Error: missing name -> VALIDATION_ERROR with fieldErrors.
4) Admin creates Crews and assigns a Foreman + Workers (roster) -> system saves roster.
5) Admin sets Company Settings:
   - dispatch authority (see Section 6)
   - payroll frequency and cutoff
   - standby cutoff time
   - default language
6) System is ready for daily operations.

### 4.2 Daily crew call (Dispatch / Worker handshake)
1) Authorized sender (Admin or Foreman, per dispatch authority) creates Crew Call:
   - selects Crew, Site, Start time, Meet point
2) System validates sender authorization and crew ownership.
   - Error: forbidden (dispatch not permitted) -> FORBIDDEN.
3) System generates a unique recipient link token per worker (stored hashed).
4) System sends outbound SMS to each worker with:
   - company name, sender name, site/time/meet point, stateful link
   - Error: SMS provider failure -> system records send failed status per recipient and surfaces to sender UI.
5) Worker opens link:
   - system resolves token -> returns Crew Call details + allowed actions
   - Error: token invalid/expired -> NOT_FOUND and renders worker-friendly expired state.

### 4.3 Worker acknowledgment (Confirm / Late / Cant / Need change)
1) Worker taps one of:
   - Confirm
   - Late -> selects ETA bucket (e.g., 10/15/30/60 minutes)
   - Cant
   - Need change
2) System records handshake status with timestamp.
   - Success: worker sees immediate confirmation state (Confirmed / Late by ~15 min).
   - Error: token expired -> NOT_FOUND, show resend instructions.

3) If worker chooses Cant or Need change:
   - worker selects availability:
     - After 9 / After 10 / After 12 / Not today
     - Different site OK (yes/no)
     - Optional note
   - system records availability and places worker in Not scheduled right now state for that day.

### 4.4 Stateful link time capture (Check-in / Check-out)
1) Worker opens the same link during the check-in window -> system returns primary CTA = Check In.
2) Worker taps Check In -> system:
   - creates or loads existing Time Entry (idempotent)
   - sets checkInAt if not already set
   - returns checkInAt
   - Error: check-in outside allowed window (policy-defined) -> VALIDATION_ERROR.

3) Later worker opens the same link -> system returns primary CTA = Check Out.
4) Worker taps Check Out -> system:
   - sets checkOutAt if not already set (idempotent)
   - returns checkOutAt
   - Error: check-out without check-in -> VALIDATION_ERROR.

### 4.5 Roll call fallback (Foreman)
1) Foreman opens Today Board -> system returns roster + statuses:
   - confirmed, late, cant, unconfirmed, checked in/out, exceptions preview
2) Foreman opens Roll Call and marks Present/Late/Absent for each worker.
3) System updates/creates Time Entries:
   - Present creates a Time Entry if missing and sets checkInAt (source = FOREMAN_ROLL_CALL).
   - Absent may mark as no-show status for exception handling.
4) System surfaces unresolved items as Exceptions (missing check-out, review request, etc.).

### 4.6 Exceptions resolution (Foreman/Admin)
1) Foreman/Admin opens Exceptions list -> system returns OPEN exceptions for selected crew/date.
2) User resolves exception with one action:
   - Approve as-is
   - Adjust time (requires reason + optional note)
   - Mark no-show/off (requires reason)
3) System:
   - applies resolution
   - writes Audit Log event (before/after + reason + actor)
   - updates Time Entry status accordingly
4) Worker timecard reflects Edited marker when time adjusted.

### 4.7 Worker My Hours + Review Request
1) Worker opens My Hours link (token-based) -> system returns weekly timecard.
2) Worker taps Request Review on a day entry -> system shows reason list + note.
3) Worker submits -> system creates Review Request and corresponding Exception.
4) Foreman/Admin resolves -> worker timecard updates status.

### 4.8 Payroll export (Admin)
1) Admin opens Payroll Period Summary -> system calculates payroll period boundaries from settings and returns:
   - totals, unresolved exceptions count, readiness indicator
2) Admin exports CSV -> system generates CSV with defined columns and downloads.
   - Error: unresolved exceptions exist -> system still allows export but includes status flags per row (Approved/Pending/Needs Review).

---

## 5) Functional Requirements (Source of Truth)

### Domain A: Multi-tenant identity, membership, and company context
A1. Company as tenant boundary
- What it does: Ensures data isolation per company.
- Access: Admin/Foreman authenticated; Worker token-scoped.
- Preconditions: activeCompanyId set in session for authenticated requests.
- Success outcome: no cross-tenant reads or writes possible.
- Failure conditions: missing/invalid company context -> UNAUTHORIZED or FORBIDDEN.

A2. Identity by phone + Membership per company
- What it does: One phone identity can belong to multiple companies.
- Access: Admin for managing membership via People workflows.
- Preconditions: unique phone in Identity.
- Success: membership created/activated with role(s).
- Failure: phone invalid -> VALIDATION_ERROR; duplicate membership constraint -> CONFLICT.

A3. Active company selection
- What it does: user selects which company they are operating within.
- Access: Admin/Foreman.
- Preconditions: user has >= 1 membership.
- Success: session sets activeCompanyId.
- Failure: company not in memberships -> FORBIDDEN.

### Domain B: Roster management (Admin-controlled)
B1. Workers management
- What it does: create/edit/deactivate worker profiles; set language; assign to crew.
- Access: Admin.
- Preconditions: valid phone; identity exists/created.
- Success: worker profile active and rostered.
- Failure: invalid phone/name -> VALIDATION_ERROR.

B2. Foremen management
- What it does: create/edit/deactivate foreman profiles.
- Access: Admin.
- Preconditions: valid phone.
- Success: foreman can log in via OTP and access crew-scoped data.
- Failure: invalid phone -> VALIDATION_ERROR.

B3. Crews management (Roster authority)
- What it does: create crews, assign foreman, assign workers.
- Access: Admin.
- Preconditions: workers exist and active.
- Success: crew roster is source of truth for recipients when sending Crew Calls.
- Failure: assigning inactive worker -> VALIDATION_ERROR.

B4. Sites management
- What it does: create/edit/activate/deactivate sites.
- Access: Admin.
- Preconditions: name required.
- Success: sites available in Crew Call send flow.
- Failure: missing name -> VALIDATION_ERROR.

### Domain C: Crew Calls (Dispatch/call-sheet)
C1. Create and send Crew Call
- What it does: sends where/when/meet point to a crew roster via SMS.
- Access: Admin always; Foreman only when permitted by authority model (Section 6).
- Preconditions: crew exists, site exists, start time provided.
- Success: Crew Call created; recipients created; SMS sent/queued; Today Board updates.
- Failure:
  - unauthorized sender -> FORBIDDEN
  - SMS provider failure -> partial failure recorded per recipient; overall request returns success with failure details.

C2. Crew Call recipient stateful link (Pattern A)
- What it does: one worker link supports handshake + check-in/out + receipt.
- Access: Worker token-scoped.
- Preconditions: valid token not expired.
- Success: worker sees correct state and available actions.
- Failure: token invalid/expired -> NOT_FOUND.

C3. Worker handshake (Confirm/Late/Cant/Need change)
- What it does: records worker intent and reduces morning chasing.
- Access: Worker token-scoped.
- Preconditions: valid token; crew call active.
- Success: handshake status updated with timestamp.
- Failure: crew call canceled -> CONFLICT; token expired -> NOT_FOUND.

C4. Availability counteroffer (after Cant/Need change)
- What it does: captures available later without promising placement.
- Access: Worker token-scoped.
- Preconditions: worker selected Cant or Need change.
- Success: availability stored; worker sees closure state Not scheduled right now.
- Failure: invalid availability value -> VALIDATION_ERROR.

C5. Targeted update to a single worker (Reassign within crew/day)
- What it does: allows foreman/admin to send updated site/time/meet point for one recipient.
- Access: Admin; Foreman only if permitted and only within their crew scope.
- Preconditions: worker is in crew roster and is a recipient for the day.
- Success: recipient override stored; worker receives update SMS; worker link displays updated details.
- Failure: cross-crew reassignment attempt by foreman -> FORBIDDEN.

### Domain D: Time capture and receipts
D1. Worker Check In
- What it does: records start time with one tap; idempotent.
- Access: Worker token-scoped.
- Preconditions: token valid; check-in allowed for that call/day.
- Success: time entry exists with checkInAt; response returns timestamp.
- Failure: outside allowed window -> VALIDATION_ERROR.

D2. Worker Check Out
- What it does: records end time with one tap; idempotent.
- Access: Worker token-scoped.
- Preconditions: checkInAt exists.
- Success: time entry updated with checkOutAt.
- Failure: no checkInAt -> VALIDATION_ERROR.

D3. Roll Call (attendance fallback)
- What it does: foreman marks present/late/absent; creates time entries if needed.
- Access: Foreman/Admin (crew scope for foreman).
- Preconditions: crew roster exists.
- Success: time entries created/updated; statuses reflected on Today Board.
- Failure: worker not on roster -> VALIDATION_ERROR.

### Domain E: Exceptions, edits, audit, and disputes
E1. Exceptions generation
- What it does: surfaces missing check-in/out and open review requests.
- Access: Foreman/Admin.
- Preconditions: time entries exist or expected.
- Success: exception list identifies actionable items.
- Failure: none (exceptions are derived; empty list is valid).

E2. Time adjustment (reason required)
- What it does: allows correcting time entries while preserving trust.
- Access: Foreman/Admin (crew scope for foreman).
- Preconditions: time entry exists.
- Success: time updated; audit log appended; worker sees edited indicator.
- Failure: invalid time range -> VALIDATION_ERROR.

E3. Approve vs Pending vs Needs Review statuses
- What it does: supports payroll readiness.
- Access: Admin; Foreman approval is disabled in v1 (explicit decision).
- Preconditions: time entry exists.
- Success: status transitions recorded with actor.
- Failure: foreman attempts approve -> FORBIDDEN.

E4. Worker review request
- What it does: structured dispute intake.
- Access: Worker token-scoped.
- Preconditions: timecard token valid.
- Success: review request created; exception generated.
- Failure: missing reason -> VALIDATION_ERROR.

E5. Audit log
- What it does: immutable record of sensitive actions.
- Access: Admin.
- Preconditions: audit events created on sensitive actions.
- Success: admin can filter and view changes by actor/entity/date.
- Failure: none; empty list valid.

### Domain F: Payroll
F1. Payroll period calculation
- What it does: derives payroll periods from company settings (weekly or biweekly).
- Access: Admin.
- Preconditions: payroll settings exist.
- Success: period boundaries computed consistently.
- Failure: invalid settings -> VALIDATION_ERROR.

F2. Payroll summary
- What it does: shows totals and unresolved exceptions for the period.
- Access: Admin.
- Preconditions: time entries exist.
- Success: admin sees readiness and can act on exceptions.
- Failure: none; empty state valid.

F3. Payroll CSV export
- What it does: exports a payroll-ready file without integrations.
- Access: Admin.
- Preconditions: period computed.
- Success: CSV downloads with defined schema and status flags.
- Failure: none; export allowed even if unresolved exceptions exist.

### Domain G: Messaging (Outbound SMS only)
G1. SMS delivery abstraction
- What it does: sends messages via provider interface with dev noop provider.
- Access: System service called by crew call workflows.
- Preconditions: worker phone exists.
- Success: message sent or marked failed per recipient.
- Failure: provider error -> captured and surfaced.

G2. Standby cutoff closure message
- What it does: reduces worker uncertainty when they offered availability.
- Access: System scheduled job.
- Preconditions: worker submitted availability for a day and no targeted update assigned by standby cutoff time.
- Success: worker receives No assignment today SMS; state updated to OFF for that day.
- Failure: provider error -> captured; does not block other flows.

---

## 6) Permissions and Authority Model

### 6.1 Authority boundaries (explicit decisions)
- Admin controls roster (workers, crews, sites). Foreman cannot modify roster.
- Dispatch is controlled by a company setting `dispatchAuthority` with a fixed behavior:
  - HYBRID is the only supported authority model in v1:
    - Admin can send crew calls for any crew.
    - Foreman can send crew calls only for crews they lead.
    - Every Crew Call is labeled with sender identity and timestamp.
    - The most recently sent call for the same crew/date is the active instruction.
- Foreman can resolve exceptions and adjust time with reason.
- Foreman cannot approve time for payroll in v1.
- Workers act only via token; no authenticated worker portal exists in v1.

### 6.2 Action-level permissions
Admin can:
- Manage workers/foremen/crews/sites/settings
- Send/update crew calls for any crew
- View and resolve exceptions
- Adjust time (reason required)
- Approve time
- Export payroll CSV
- View audit log

Foreman can (crew scope only):
- View Today Board and crew call statuses
- Send/update crew calls for their crew
- Submit roll call
- Resolve exceptions
- Adjust time (reason required)
- Send targeted updates to a single worker within their crew/day (recipient override)

Foreman cannot:
- Change roster
- Move workers across crews
- Approve time for payroll
- Edit company settings

Worker can (token-scoped):
- View crew call details
- Submit handshake + availability
- Check in/out
- View timecard via timecard token
- Submit review request

Worker cannot:
- View other workers
- Edit time entries directly
- Access authenticated portals

---

## 7) Data Model (Conceptual)

### Entities and purpose
- Company
  - Represents a tenant.
  - Fields: name, defaultLanguage, payrollFrequency, payrollCutoff, standbyCutoffTime, dispatchAuthority
- Identity
  - Global unique person by phone.
  - Fields: phoneE164
- Membership
  - Identity within a company + roles.
  - Fields: companyId, identityId, roles, status
- WorkerProfile
  - Company-scoped worker info.
  - Fields: membershipId, displayName, preferredLanguage, crewId, active
- ForemanProfile
  - Company-scoped foreman info.
  - Fields: membershipId, displayName, active
- Crew
  - Roster unit.
  - Fields: name, foremanMembershipId
- Site
  - Job site reference.
  - Fields: name, address, notes, active
- CrewCall
  - The call-sheet: crew + site + start + meet point + sender.
  - Fields: crewId, siteId, startAt, meetPoint, sentByMembershipId, status
- CrewCallRecipient
  - Per-worker linkage + stateful token + handshake state.
  - Fields: crewCallId, workerMembershipId, tokenHash, expiresAt, handshakeStatus, lateEta, availability, overrideSiteId/overrideStartAt/overrideMeetPoint
- TimeEntry
  - Daily time receipt for a worker for a crew call.
  - Fields: workerMembershipId, crewCallId, workDate, checkInAt, checkOutAt, sources, status, edited, editReason
- ReviewRequest
  - Worker-submitted dispute intake.
  - Fields: workerMembershipId, workDate, reason, note, status
- Exception
  - Actionable operational issue (missing punch, review request).
  - Fields: workerMembershipId, crewCallId, date, type, status, resolution
- AuditLog
  - Immutable record of sensitive actions.
  - Fields: actorMembershipId, actionType, entityType, entityId, detailsJson, createdAt

### Relationships
- Company 1..* Membership
- Identity 1..* Membership
- Crew 1..* WorkerProfile
- Crew 1..* CrewCall
- CrewCall 1..* CrewCallRecipient
- CrewCallRecipient 0..1 TimeEntry (per day)
- Worker 1..* TimeEntry, ReviewRequest, Exceptions
- TimeEntry/CrewCall changes 1..* AuditLog events

---

## 8) API Surface (High-Level)

All endpoints are REST under `/api/v1`. Business logic lives in service layer.

### Auth and session
- `POST /auth/otp/start`
- `POST /auth/otp/verify`
- `POST /auth/logout`
- `GET /me`
- `POST /me/active-company`

### Admin resources
- `GET/POST/PUT /admin/workers`
- `GET/POST/PUT /admin/foremen`
- `GET/POST/PUT /admin/crews`
- `GET/POST/PUT /admin/sites`
- `GET/PUT /admin/settings`
- `GET /admin/payroll/periods/current`
- `GET /admin/payroll/periods/{id}`
- `GET /admin/payroll/periods/{id}/export` (CSV)
- `GET /admin/audit`

### Foreman resources (crew-scoped)
- `GET /foreman/today?date=&crewId=`
- `POST /foreman/crew-calls` (create/send)
- `POST /foreman/crew-calls/{id}/resend` (update/send)
- `POST /foreman/roll-call`
- `GET /foreman/exceptions?date=&crewId=`
- `POST /foreman/exceptions/{id}/resolve`
- `POST /foreman/time-adjustments`
- `POST /foreman/recipient-overrides` (targeted update to a single worker)

### Worker public resources (token-scoped)
- `GET /public/worker/links/{token}` (route resolution)
- `GET /public/worker/crew-calls/by-link/{token}`
- `POST /public/worker/crew-calls/by-link/{token}/handshake`
- `POST /public/worker/crew-calls/by-link/{token}/availability`
- `POST /public/worker/crew-calls/by-link/{token}/check-in`
- `POST /public/worker/crew-calls/by-link/{token}/check-out`
- `GET /public/worker/timecard/by-link/{token}?week=`
- `POST /public/worker/timecard/by-link/{token}/review-requests`

Error handling contract (all endpoints):
- returns `{ errorCode, message, fieldErrors? }`
- no stack traces

---

## 9) UX and UI Requirements

### UX principles
- Worker UX must be no training required.
- One primary action per state on worker link.
- Foreman UX must be exception-driven: If nothing is wrong, you are done.
- Admin UX must support payroll readiness: see unresolved items quickly.

### Accessibility requirements (WCAG AA minimum)
- Keyboard navigation for authenticated portals (Admin/Foreman)
- Visible focus states
- Contrast meets AA
- Labels on all inputs
- Error messages associated with fields
- Screen reader-friendly status announcements for critical actions (checked in/out, submitted, errors)

### Required UI states (must be implemented everywhere)
- Loading: skeleton or spinner with accessible label
- Empty: explicit empty state with next action
- Error: friendly message + retry when applicable
- Partial failure (SMS sending): show per-recipient send failures to sender

### Design system requirements (token-based)
- No ad-hoc colors/spacing/typography in components.
- Use design tokens for:
  - color palette (semantic tokens: bg/text/border/status)
  - spacing scale
  - typography scale
  - radii
  - elevation/shadows
- UI primitives must be reusable and consistent across screens.
- All form patterns use consistent validation and error rendering.

---

## 10) MVP Scope Lock

### Ships in v1 (must be complete)
- Multi-tenant companies with identity/membership and active company selection
- Admin roster management (workers, foremen, crews, sites, settings)
- Crew Calls (create/send/update) with outbound SMS
- Worker stateful link:
  - handshake + availability + closure
  - check-in/out
  - completion receipt state
- Foreman Today Board + roll call fallback
- Exceptions list + resolution workflow
- Time adjustments with reason + audit log
- Worker My Hours + review request
- Payroll period summary + CSV export
- Standby cutoff closure message for availability-with-no-assignment

### Explicitly does not ship in v1
- Task assignment
- Payroll provider integrations
- Cost codes/job costing
- Open shift marketplace
- Continuous GPS or photo verification
- Segmenting a day into multiple jobsite time splits

### MVP invalidation rule (prevents scope creep)
Adding any of the following invalidates the MVP by expanding scope beyond a shippable core:
- task assignment/work tracking
- cost codes/job costing
- payroll integrations
- continuous tracking features
- multi-segment time entries per day

---

## 11) Success Metrics

### User success metrics
- Crew Call send rate per active crew/day (target 70% after 2 weeks)
- Worker link open rate (target 60%)
- Worker acknowledgment rate (target 50%)
- Time capture rate via check-in/out or roll call (target 75%)
- Review request rate stabilizes (initial spike acceptable; then trends downward)

### System health metrics
- API error rate by endpoint
- SMS send failure rate
- Median worker link load time
- Median time to resolve exceptions
- Idempotency integrity (duplicate time entries rate should be near zero)

### MVP success after launch (definition)
After 2 payroll cycles in pilot accounts:
- Companies use Crew Calls consistently (70% workdays)
- Payroll export is used (1 export per period)
- Reported payroll disputes decrease vs baseline
- Foremen report reduced morning chasing and fewer where are you calls

---

## 12) Risks and Mitigations

### Product risks
1) Workers perceive surveillance and refuse use
- Mitigation: no continuous GPS; explicit trust language; worker-visible timecard; audit trail for edits.

2) Foremen avoid any admin work
- Mitigation: worker no-login; one stateful link; roll call is quick; foreman only handles exceptions list.

3) Office fears foremen dispatching improperly
- Mitigation: admin controls roster; sender labeled on every crew call; audit events; foreman limited to crew scope.

4) Low smartphone access / low compliance
- Mitigation: roll call fallback; time capture does not require every worker to use link.

### Technical risks
1) Cross-tenant data leakage
- Mitigation: companyId scoping in every repository/service; automated tests that attempt cross-tenant access; token resolution always returns company-bound context.

2) Token compromise or misuse
- Mitigation: store token hashes only; expire tokens; do not log tokens; rate-limit public endpoints.

3) SMS deliverability and provider outages
- Mitigation: provider abstraction + retries; per-recipient failure visibility; noop provider in dev; message length control.

4) Idempotency bugs (duplicate punches)
- Mitigation: unique constraints for time entries; idempotent handlers; integration tests for double taps.

### Operational risks
1) Support burden during onboarding
- Mitigation: admin setup flow is minimal; seed templates; simple CSV import optional but not required; clear help content.

2) Payroll period confusion across companies
- Mitigation: company-specific settings; clear period labeling and cutoff rules; export includes statuses and edited flags.
