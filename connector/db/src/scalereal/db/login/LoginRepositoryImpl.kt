package scalereal.db.login

import jakarta.inject.Inject
import jakarta.inject.Singleton
import login.AddPasswordCommand
import login.AddPasswordParams
import login.GetMaxOrganisationIdParams
import login.GetMaxOrganisationIdQuery
import norm.command
import norm.query
import scalereal.core.login.LoginRepository
import javax.sql.DataSource

@Singleton
class LoginRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : LoginRepository {
    override fun setPassword(
        password: String,
        emailId: String,
    ): Unit =
        dataSource.connection.use { connection ->
            AddPasswordCommand()
                .command(
                    connection,
                    AddPasswordParams(password = password, email_id = emailId),
                )
        }

    override fun getMaxOrganisationId(): Int =
        dataSource.connection.use { connection ->
            val maxOrganisationId =
                GetMaxOrganisationIdQuery()
                    .query(
                        connection,
                        GetMaxOrganisationIdParams(),
                    ).map { it.maxId }
            return maxOrganisationId.firstOrNull() ?: 0
        }
}
