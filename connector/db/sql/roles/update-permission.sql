UPDATE
 module_permissions
SET
 view = :view,
 edit = :edit
WHERE
 role_id = :roleId
 AND module_id = :moduleId ;
