INSERT INTO kpi(organisation_id, kpi_id, title, description, status)
 VALUES (:organisationId, :kpiId, :kpiTitle, :kpiDescription, :kpiStatus) RETURNING *;
