SELECT id FROM departments WHERE
      organisation_id = :organisationId
      AND LOWER(department_name) = LOWER(:departmentName)
      AND departments.status = true;