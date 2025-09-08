SELECT EXISTS (
  SELECT 1 FROM kra WHERE
     LOWER(name) = LOWER(:kra) AND organisation_id = :organisationId
) ;