INSERT INTO refresh_tokens (user_id, refresh_token, revoked)
VALUES (:user_id, :refresh_token, :revoked) RETURNING *