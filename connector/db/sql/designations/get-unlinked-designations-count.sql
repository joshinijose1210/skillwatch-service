SELECT
  COUNT(designations.designation_id)
  AS unlinked_designation_count
FROM designations
  LEFT JOIN team_designation_mapping tdm ON tdm.designation_id = designations.id
  LEFT JOIN teams t ON t.id = tdm.team_id
WHERE designations.organisation_id = :organisationId
  AND tdm.designation_id IS NULL
  AND designations.designation_name != 'Org Admin' ;