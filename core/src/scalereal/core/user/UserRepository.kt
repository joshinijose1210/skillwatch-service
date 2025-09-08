package scalereal.core.user

import scalereal.core.models.domain.User

interface UserRepository {
    fun finBy(userName: String): User?

    fun getById(id: Long): User?

    fun isUserExist(emailId: String): Boolean

    fun createUser(user: User)

    fun getRole(userName: Any): List<String>

    fun getUser(emailId: String): User

    fun getSecret(email: String): String

    fun isSuperAdmin(email: String): Boolean

    fun fetchSuperAdminEmailIds(): List<String>
}
