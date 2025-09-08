WITH subquery AS (
    SELECT status
    FROM roles
    WHERE
    LOWER(role_name) = LOWER(:roleName)
    AND organisation_id = :organisationId
)
SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery); 