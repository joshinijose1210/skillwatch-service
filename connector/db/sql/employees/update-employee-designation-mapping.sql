UPDATE
  employees_designation_mapping
SET
  emp_id = :id,
  designation_id = :designationId
WHERE
  emp_id = :id;