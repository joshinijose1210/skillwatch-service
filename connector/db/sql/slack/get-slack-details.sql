SELECT
   id,
   access_token,
   channel_id,
   webhook_url
FROM
   slack_details
WHERE
   organisation_id = :organisationId ;
