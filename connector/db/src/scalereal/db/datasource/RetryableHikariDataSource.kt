package scalereal.db.datasource

import com.zaxxer.hikari.HikariDataSource
import io.micronaut.context.annotation.Context
import org.slf4j.LoggerFactory
import scalereal.db.datasource.config.DatabaseConfiguration
import scalereal.db.datasource.config.HikariConfiguration
import java.sql.Connection
import javax.annotation.PreDestroy

@Context
class RetryableHikariDataSource(
    hikariConfiguration: HikariConfiguration,
    databaseConfiguration: DatabaseConfiguration,
) : HikariDataSource() {
    private val log = LoggerFactory.getLogger(this::class.java.simpleName)

    init {
        leakDetectionThreshold = hikariConfiguration.leakDetectionThreshold
        maximumPoolSize = hikariConfiguration.maximumPoolSize
        driverClassName = "org.postgresql.Driver"

        jdbcUrl = databaseConfiguration.url
        username = databaseConfiguration.username
        password = databaseConfiguration.password
        driverClassName = databaseConfiguration.driverClassName
    }

    override fun getConnection(): Connection =
        try {
            super.getConnection()
        } catch (e: Exception) {
            log.info("Authentication Error, Going to update secret and try again")
            // Write update credentials code here
            log.info("Retrying with updated secrets")
            super.getConnection()
        }

    @PreDestroy
    override fun close() {
        runCatching {
            super.close()
        }
    }
}
