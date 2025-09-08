UPDATE
  departments
SET
  department_name = :departmentName,
  status = :departmentStatus,
  updated_at = CURRENT_TIMESTAMP
WHERE
  organisation_id = :organisationId
  AND id = :id ;