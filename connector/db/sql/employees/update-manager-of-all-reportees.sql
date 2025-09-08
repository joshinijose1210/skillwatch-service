UPDATE employee_manager_mapping
SET manager_id = CASE
    WHEN manager_id = :currentManagerId AND type = 1 THEN :newManagerId
    WHEN manager_id = :currentManagerId AND type = 2 THEN :newManagerId
    ELSE manager_id
  END
WHERE emp_id = :employeeId;