SELECT
eh.id,
eh.employee_id,
eh.activated_at,
eh.deactivated_at
FROM employees_history eh
WHERE eh.employee_id = ANY (:employeeId::INT[]);