SELECT id , first_name, last_name, email_id
FROM users
WHERE LOWER(email_id) = LOWER(:emailId);