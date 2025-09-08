SELECT COUNT(roles.role_id) AS role_count
FROM
  roles
WHERE
  organisation_id = :organisationId
  AND (cast(:searchText as text) IS NULL
  OR UPPER(roles.role_name) LIKE UPPER(:searchText)) ;