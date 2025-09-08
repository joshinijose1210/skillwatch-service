SELECT id FROM employees WHERE
      LOWER(emp_id) = LOWER(:employeeId)
      AND organisation_id = :organisationId;