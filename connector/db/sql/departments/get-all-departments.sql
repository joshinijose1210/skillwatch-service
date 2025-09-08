SELECT
  id,
  organisation_id,
  department_id,
  department_name,
  created_at,
  updated_at,
  status
FROM
  departments
WHERE
  organisation_id = :organisationId
  AND (cast(:searchText as text) IS NULL
  OR UPPER(departments.department_name) LIKE UPPER(:searchText))
ORDER BY
  department_id DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);