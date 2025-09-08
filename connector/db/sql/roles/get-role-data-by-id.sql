SELECT
    id,
    role_id,
    role_name,
    status
FROM roles
WHERE
id = :id
AND organisation_id = :organisationId ;