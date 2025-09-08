INSERT INTO departments(department_name, status, department_id, organisation_id)
VALUES
  (:departmentName, :departmentStatus, :id, :organisationId)
RETURNING id;