UPDATE
  employees
SET
  onboarding_flow = :onboarding_flow
where
  id = :id
  AND organisation_id = :organisationId ;
