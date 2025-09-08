UPDATE
  employee_manager_mapping
SET
  updated_at = now(),
  is_active = false
WHERE
  emp_id = :id
  AND type = :managerType
  AND is_active = true;
