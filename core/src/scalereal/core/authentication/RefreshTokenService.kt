package scalereal.core.authentication

import jakarta.inject.Inject
import jakarta.inject.Singleton
import scalereal.core.employees.EmployeeRepository
import scalereal.core.exception.UserNotFoundException
import scalereal.core.models.RefreshTokenEntity
import scalereal.core.models.domain.EmpData
import scalereal.core.models.domain.User
import scalereal.core.user.UserRepository
import java.util.Base64
import java.util.UUID

@Singleton
class RefreshTokenService(
    @Inject private val refreshTokenRepository: RefreshTokenRepository,
    @Inject private val userRepository: UserRepository,
    @Inject private val employeeRepository: EmployeeRepository,
) {
    fun save(
        userName: String,
        token: String,
        revoked: Boolean,
    ): RefreshTokenEntity = refreshTokenRepository.save(getUser(userName).id, token, revoked)

    fun findByRefreshToken(refreshToken: String): Pair<EmpData, RefreshTokenEntity?> {
        val token = refreshTokenRepository.findByRefreshToken(refreshToken)
        val user = employeeRepository.getEmployeeById(token?.userId)
        return Pair(user, token)
    }

    fun updateByUsername(
        userName: String,
        revoked: Boolean,
    ): Long = refreshTokenRepository.updateByUsername(getUser(userName).id, revoked)

    fun revokeRefreshToken(
        userId: Long,
        refreshToken: String,
    ) = refreshTokenRepository.revokeRefreshToken(userId, refreshToken)

    fun convertToUuidFormat(refreshToken: String): String {
        val uuidStartIndex = refreshToken.indexOf('.') + 1
        val uuidEndIndex = refreshToken.lastIndexOf('.')
        val uuidString = refreshToken.substring(uuidStartIndex, uuidEndIndex)

        val decodedUuidBytes = Base64.getDecoder().decode(uuidString)
        val uuid = UUID.fromString(String(decodedUuidBytes))

        return uuid.toString()
    }

    private fun getUser(userName: String): User =
        userRepository.finBy(userName) ?: throw UserNotFoundException("User not found by $userName")
}
