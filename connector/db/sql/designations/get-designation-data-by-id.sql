SELECT
  d.id,
  d.organisation_id,
  d.designation_id AS designation_display_id,
  COALESCE(dtm.department_id, null) AS department_id,
  COALESCE(dp.department_name, null) AS department_name,
  COALESCE(dp.department_id, null) AS department_display_id,
  COALESCE(dp.status, null) AS department_status,
  COALESCE(tdm.team_id, null) AS team_id,
  COALESCE(t.team_name, null) AS team_name,
  COALESCE(t.team_id, null) AS team_display_id,
  COALESCE(t.status, null) AS team_status,
  d.designation_name,
  d.status,
  d.created_at,
  d.updated_at
FROM designations d
  LEFT JOIN team_designation_mapping tdm ON tdm.designation_id = d.id
  LEFT JOIN teams t ON t.id = tdm.team_id
  LEFT JOIN department_team_mapping dtm ON dtm.team_id = t.id
  LEFT JOIN departments dp ON dp.id = dtm.department_id
WHERE d.id = :id
  AND d.organisation_id = :organisationId;