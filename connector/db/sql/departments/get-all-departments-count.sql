SELECT COUNT(departments.department_id) AS department_count
FROM departments
WHERE organisation_id = :organisationId
    AND (cast(:searchText as text) IS NULL
    OR UPPER(departments.department_name) LIKE UPPER(:searchText)) ;