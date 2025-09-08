select employees_role_mapping.role_id,
 roles.role_name
 from employees_role_mapping
JOIN employees on employees.id = employees_role_mapping.emp_id
JOIN roles ON employees_role_mapping.role_id = roles.id
where LOWER(employees.email_id) = LOWER(:emailId) ;
