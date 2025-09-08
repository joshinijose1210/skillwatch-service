package user

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.db.user.UserRepositoryImpl
import util.StringSpecWithDataSource

class UserRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var userRepositoryImpl: UserRepositoryImpl

    init {
        "should find user by id " {
            val user = userRepositoryImpl.getById(1)

            user?.id shouldBe 1
            user?.firstName shouldBe "sherlock"
            user?.lastName shouldBe "holmes"
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                insert into users (id, first_name, last_name, email_id, password)
                VALUES (1, 'sherlock', 'holmes', 'sherlock', 'password');
                
                """.trimIndent(),
            )
        }
        userRepositoryImpl = UserRepositoryImpl(dataSource)
    }
}
