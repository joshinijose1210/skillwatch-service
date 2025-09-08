INSERT INTO employees (emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
VALUES (:empId, :firstName, :lastName, :emailId, :contactNo, true, :organisationId) RETURNING id ;

