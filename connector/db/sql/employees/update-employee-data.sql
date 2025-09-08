UPDATE
  employees
SET
  emp_id = :employeeId,
  first_name = :firstName,
  last_name = :lastName,
  email_id = :emailId,
  contact_no = :contactNo,
  status = :status,
  gender_id = :genderId,
  date_of_birth= :dateOfBirth,
  date_of_joining = :dateOfJoining,
  experience = :experience,
  is_consultant = :isConsultant,
  updated_at = CURRENT_TIMESTAMP
WHERE
 id = :id
 AND organisation_id = :organisationId
RETURNING * ;

