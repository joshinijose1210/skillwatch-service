SELECT
  module_id,
  modules.name AS module_name,
  view,
  edit
from
  module_permissions
JOIN modules on modules.id = module_permissions.module_id
where
  role_id = :roleId;
