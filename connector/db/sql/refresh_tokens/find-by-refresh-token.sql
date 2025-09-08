SELECT
   id, user_id, refresh_token, revoked, created_at, updated_at
FROM
   refresh_tokens
WHERE
   refresh_token = :refresh_token;
