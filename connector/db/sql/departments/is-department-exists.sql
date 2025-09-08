WITH subquery AS (
    SELECT status
    FROM departments
    WHERE
    organisation_id = :organisationId
    AND LOWER(department_name) = LOWER(:departmentName)
)
SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);