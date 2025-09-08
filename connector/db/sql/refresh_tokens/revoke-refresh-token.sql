UPDATE refresh_tokens
SET revoked = true, updated_at = now()
WHERE user_id = :userId
AND refresh_token = :refreshToken;