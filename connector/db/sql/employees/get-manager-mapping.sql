SELECT
    MAX(CASE WHEN emp_id = :id AND type = 1 THEN manager_id END) AS first_manager_id,
    MAX(CASE WHEN emp_id = :id AND type = 2 THEN manager_id END) AS second_manager_id
FROM employee_manager_mapping_view ;
