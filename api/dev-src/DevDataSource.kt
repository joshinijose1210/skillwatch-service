package cps.api

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Replaces
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

@Context
@Replaces(DataSource::class)
class DevDataSource : PGSimpleDataSource() {
    init {
        setURL("jdbc:postgresql://localhost/sample")
        user = "postgres"
        password = "password"
    }
}
