INSERT INTO designations(designation_id, organisation_id, designation_name, status)
VALUES
(:designationId, :organisationId, :designationName,:status) RETURNING id;