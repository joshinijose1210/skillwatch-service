INSERT INTO slack_details(
  organisation_id, access_token, channel_id, webhook_url, workspace_id
)
VALUES (
    :organisationId,
    :accessToken,
    :channelId,
    :webhook_url,
    :workspaceId
 ) ;
