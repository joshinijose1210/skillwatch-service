SELECT CASE WHEN (
    SELECT COUNT(*) FROM employees
      JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
      AND firstManagerMapping.type = 1
      LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
      AND secondManagerMapping.type = 2
      LEFT JOIN review_details ON employees.id  = review_details.review_to
      AND review_details.review_type_id = 2
      AND review_details.review_cycle_id = :review_cycle_id
      AND review_details.review_from = :manager_employee_id
    WHERE
      ((firstManagerMapping.manager_id = :manager_employee_id AND firstManagerMapping.emp_id != :manager_employee_id) OR
      (secondManagerMapping.manager_id = :manager_employee_id AND secondManagerMapping.emp_id != :manager_employee_id))
      AND employees.status = TRUE
      AND ( review_details.published IS NULL OR review_details.published = FALSE )
) > 0 THEN false ELSE true END AS result ;
