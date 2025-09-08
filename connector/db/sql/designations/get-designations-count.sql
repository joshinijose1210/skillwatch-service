SELECT
COUNT(designations.designation_id)
as designation_count
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
  AND (:departmentId::INT[] = '{-99}' OR dtm.department_id = ANY (:departmentId::INT[]));