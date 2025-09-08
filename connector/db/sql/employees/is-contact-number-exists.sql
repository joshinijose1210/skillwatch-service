SELECT EXISTS (
  SELECT 1 FROM employees WHERE
      contact_no = :contactNo
      AND organisation_id = :organisationId
) ;
