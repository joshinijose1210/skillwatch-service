INSERT INTO roles(organisation_id, role_id, role_name, status)
VALUES
(:organisationId, :roleId, :roleName,:status) RETURNING id ;