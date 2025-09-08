UPDATE employees_history
SET deactivated_at = :deactivatedAt
WHERE employee_id = :employeeId
AND id = (
  SELECT MAX(id)
  FROM employees_history
  WHERE employee_id = :employeeId
);
