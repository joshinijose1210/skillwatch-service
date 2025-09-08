UPDATE
goals
SET
description =:description
WHERE
 goals.id = :id
RETURNING *;
