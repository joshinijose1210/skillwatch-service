package scalereal.core.login

interface LoginRepository {
    fun setPassword(
        password: String,
        emailId: String,
    )

    fun getMaxOrganisationId(): Int
}
