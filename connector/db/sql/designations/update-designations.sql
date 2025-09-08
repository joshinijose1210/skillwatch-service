UPDATE
  designations
SET
  designation_name = :designationName,
  status = :status,
  updated_at = CURRENT_TIMESTAMP
WHERE
  id = :id
  AND organisation_id = :organisationId ;
