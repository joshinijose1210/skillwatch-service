SELECT id FROM roles WHERE
      LOWER(role_name) = LOWER(:roleName)
      AND organisation_id = :organisationId
      AND roles.status = true;