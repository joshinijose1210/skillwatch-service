package util

import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import norm.executeCommand
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager

class SpecDataSource : PGSimpleDataSource() {
    init {
        Postgresql.init()
        setURL(Postgresql.container.jdbcUrl)
        user = Postgresql.container.username
        password = Postgresql.container.password
    }

    fun cleanup() {
        this.connection.use {
            it.executeCommand(
                """
                do
                ${'$'}${'$'}
                declare
                  l_stmt text;
                begin
                  select 'TRUNCATE ' || string_agg(format('%I.%I', schemaname, tablename), ',') || ' RESTART IDENTITY CASCADE'
                    into l_stmt
                  from pg_tables
                  where schemaname in ('public');
                  execute l_stmt;
                end;
                ${'$'}${'$'}
                """.trimIndent(),
            )
        }
    }
}

object Postgresql {
    class SpecPostgresqlContainer : PostgreSQLContainer<SpecPostgresqlContainer>("postgres")

    lateinit var container: SpecPostgresqlContainer

    fun init() {
        if (!this::container.isInitialized || !container.isRunning) {
            container = SpecPostgresqlContainer()

            // reuse container between tests instead of starting a new one per execution
            container
                .withReuse(true)
                .withNetwork(null)
                .start()
            val connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)

            Liquibase(
                "changelog.xml",
                FileSystemResourceAccessor("../../migration/res"),
                JdbcConnection(connection),
            ).update("connector:db:test")
        }
    }
}
