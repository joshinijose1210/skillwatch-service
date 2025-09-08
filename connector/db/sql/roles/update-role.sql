UPDATE
  roles
SET
  role_name = :roleName,
  status = :status,
  updated_at = CURRENT_TIMESTAMP
WHERE
  id = :id
  AND organisation_id = :organisationId ;
