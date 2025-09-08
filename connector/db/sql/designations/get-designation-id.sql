SELECT id FROM designations
      JOIN team_designation_mapping tdm ON designations.id = tdm.designation_id
      WHERE tdm.team_id = :teamId
      AND LOWER(designation_name) = LOWER(:designationName)
      AND organisation_id = :organisationId
      AND designations.status = true;
