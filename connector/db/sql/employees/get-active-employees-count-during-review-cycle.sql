SELECT COUNT(DISTINCT e.id) AS active_employees
FROM employees e
JOIN employees_history eh ON e.id = eh.employee_id
JOIN review_cycle rc ON DATE(eh.activated_at) <= rc.end_date
AND (DATE(eh.deactivated_at) IS NULL OR DATE(eh.deactivated_at) >= DATE(rc.start_date))
WHERE e.organisation_id = :organisationId
  AND rc.id = :reviewCycleId ;