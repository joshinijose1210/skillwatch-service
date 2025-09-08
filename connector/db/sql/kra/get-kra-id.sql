SELECT id FROM kra WHERE
      organisation_id = :organisationId
      AND LOWER(name) = LOWER(:kraName);
