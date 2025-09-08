SELECT COUNT(users.email_id) AS count
FROM users
LEFT JOIN employees ON users.email_id = employees.email_id
LEFT JOIN organisations on employees.id = organisations.admin_id
WHERE users.is_org_admin = TRUE;
