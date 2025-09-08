UPDATE employees
SET contact_no = :contactNo
FROM organisations
WHERE employees.id = organisations.admin_id
AND organisations.sr_no = :id;
