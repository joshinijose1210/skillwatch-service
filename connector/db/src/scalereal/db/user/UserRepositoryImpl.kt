package scalereal.db.user

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.models.domain.User
import scalereal.core.user.UserRepository
import user.AddNewUserCommand
import user.AddNewUserParams
import user.FetchSuperAdminEmailIdsParams
import user.FetchSuperAdminEmailIdsQuery
import user.FindUserByIdParams
import user.FindUserByIdQuery
import user.FindUserByNameParams
import user.FindUserByNameQuery
import user.GetSecretParams
import user.GetSecretQuery
import user.GetUserParams
import user.GetUserQuery
import user.GetUserRoleParams
import user.GetUserRoleQuery
import user.IsSuperAdminUserParams
import user.IsSuperAdminUserQuery
import user.IsUserExistParams
import user.IsUserExistQuery
import javax.sql.DataSource

@Singleton
class UserRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : UserRepository {
    override fun finBy(userName: String): User? =
        dataSource.connection.use { connection ->
            FindUserByNameQuery()
                .query(connection, FindUserByNameParams(userName))
                .map {
                    User(it.userid, it.firstName, it.lastName, it.username)
                }.firstOrNull()
        }

    override fun getById(id: Long): User? =
        dataSource.connection.use { connection ->
            FindUserByIdQuery()
                .query(connection, FindUserByIdParams(id))
                .map {
                    User(
                        id = it.userid,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailId = it.emailId,
                    )
                }.firstOrNull()
        }

    override fun isUserExist(emailId: String): Boolean =
        dataSource.connection.use { connection ->
            IsUserExistQuery()
                .query(connection, IsUserExistParams(emailId))
                .map { it.exists ?: false }
                .first()
        }

    override fun createUser(user: User) {
        dataSource.connection.use { connection ->
            AddNewUserCommand()
                .command(
                    connection,
                    AddNewUserParams(
                        first_name = user.firstName,
                        last_name = user.lastName,
                        email_id = user.emailId,
                    ),
                )
        }
    }

    override fun getRole(userName: Any): List<String> =
        dataSource.connection.use { connection ->
            GetUserRoleQuery()
                .query(connection, GetUserRoleParams(userName.toString()))
                .map {
                    it.roleName.uppercase()
                }
        }

    override fun getUser(emailId: String): User =
        dataSource.connection.use { connection ->
            GetUserQuery()
                .query(connection, GetUserParams(emailId))
                .map {
                    User(
                        id = it.id,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailId = it.emailId,
                    )
                }.first()
        }

    override fun getSecret(email: String): String =
        dataSource.connection.use { connection ->
            GetSecretQuery()
                .query(connection, GetSecretParams(email))
                .map { it.password.toString() }[0]
        }

    override fun isSuperAdmin(email: String): Boolean =
        dataSource.connection.use { connection ->
            IsSuperAdminUserQuery()
                .query(connection, IsSuperAdminUserParams(email))
                .map { it.exists }
                .first() ?: false
        }

    override fun fetchSuperAdminEmailIds(): List<String> =
        dataSource.connection.use { connection ->
            FetchSuperAdminEmailIdsQuery()
                .query(connection, FetchSuperAdminEmailIdsParams())
                .map { it.emailId }
        }
}
