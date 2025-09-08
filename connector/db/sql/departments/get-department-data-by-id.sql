SELECT
  organisation_id,
  id,
  department_id as display_id,
  department_name,
  status,
  created_at,
  updated_at
FROM
  departments
WHERE
  id = :id
  AND organisation_id = :organisationId ;