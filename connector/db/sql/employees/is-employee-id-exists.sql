WITH subquery AS (
    SELECT status FROM employees
    WHERE
    LOWER(emp_id) = LOWER(:employeeId)
    AND organisation_id = :organisationId
)
SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);