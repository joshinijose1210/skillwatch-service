SELECT
  organisation_id,
  id,
  role_id,
  role_name,
  status,
  created_at,
  updated_at
FROM
  roles
WHERE
  organisation_id = :organisationId
  AND (cast(:searchText as text) IS NULL
  OR UPPER(roles.role_name) LIKE UPPER(:searchText))
ORDER BY
  created_at DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);
