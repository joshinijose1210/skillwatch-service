SELECT
  designations.id,
  designations.organisation_id,
  designations.designation_id,
  COALESCE(dtm.department_id, null) AS department_id,
  COALESCE(d.department_name, null) AS department_name,
  COALESCE(d.department_id, null) AS department_display_id,
  COALESCE(d.status, null) AS department_status,
  COALESCE(tdm.team_id, null) AS team_id,
  COALESCE(t.team_name, null) AS team_name,
  COALESCE(t.team_id, null) AS team_display_id,
  COALESCE(t.status, null) AS team_status,
  designations.designation_name,
  designations.status,
  designations.created_at,
  designations.updated_at
FROM designations
  LEFT JOIN team_designation_mapping tdm ON tdm.designation_id = designations.id
  LEFT JOIN teams t ON t.id = tdm.team_id
  LEFT JOIN department_team_mapping dtm ON dtm.team_id = t.id
  LEFT JOIN departments d ON d.id = dtm.department_id
WHERE designations.organisation_id = :organisationId
  AND (cast(:searchText as text) IS NULL
  OR UPPER(designations.designation_name) LIKE UPPER(:searchText)
  OR UPPER(t.team_name) LIKE UPPER(:searchText)
  OR UPPER(d.department_name) LIKE UPPER(:searchText))
  AND (:teamId::INT[] = '{-99}' OR tdm.team_id = ANY (:teamId::INT[]))
  AND (:departmentId::INT[] = '{-99}' OR dtm.department_id = ANY (:departmentId::INT[]))
ORDER BY designations.created_at DESC
OFFSET (:offset::INT)
LIMIT (:limit::INT);
