package emails

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.emails.EmailSenderService

class EmailSenderServiceTest : StringSpec() {
    private val emailSenderService = mockk<EmailSenderService>()

    init {
        "should send an welcome email " {

            every { emailSenderService.sendEmail(any(), any(), any(), any()) } returns Unit

            emailSenderService.sendEmail(
                receiver = "abc@scalereal.com",
                subject = "test mail",
                htmlBody = "",
                textBody = "",
            )

            verify(exactly = 1) {
                emailSenderService.sendEmail(
                    receiver = "abc@scalereal.com",
                    subject = "test mail",
                    htmlBody = "",
                    textBody = "",
                )
            }
        }
    }
}
