SELECT
 view,
 edit
FROM
 module_permissions
JOIN modules ON modules.id = module_permissions.module_id
JOIN roles ON roles.id = module_permissions.role_id AND roles.organisation_id = :organisationId
WHERE
UPPER(modules.name) = UPPER(:moduleName)
AND UPPER(roles.role_name) = UPPER(:roleName) ;
