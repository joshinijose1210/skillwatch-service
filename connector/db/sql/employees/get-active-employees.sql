SELECT
 organisation_id,
 id,
 emp_id,
 first_name,
 last_name,
 email_id,
 contact_no,
 status
FROM
 employees
WHERE
 status = true
 AND organisation_id = :organisationId ;
