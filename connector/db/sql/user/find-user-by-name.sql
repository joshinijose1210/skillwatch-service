SELECT id AS userid, first_name, last_name, email_id AS username
FROM employees
WHERE LOWER(email_id) = LOWER(:username) ;