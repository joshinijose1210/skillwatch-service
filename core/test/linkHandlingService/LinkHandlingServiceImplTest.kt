package linkHandlingService

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import scalereal.core.exception.InvalidLinkException
import scalereal.core.linkHandling.LinkDetails
import scalereal.core.linkHandling.LinkHandlingRepo
import scalereal.core.linkHandling.LinkHandlingServiceImpl
import java.sql.Timestamp

class LinkHandlingServiceImplTest : StringSpec() {
    private val linkHandlingRepo = mockk<LinkHandlingRepo>()
    private val linkHandlingService = LinkHandlingServiceImpl(linkHandlingRepo)

    init {

        val uniqueLinkId = "unique_link_id"

        "should check link validation after 24 hours" {

            val linkDetails =
                LinkDetails(
                    linkId = uniqueLinkId,
                    // 19-04-2023 09:27:00 AM Approx
                    generationTime = Timestamp(1681876624),
                    noOfHit = 0,
                    purposeOfLink = "Set Password",
                )

            every { linkHandlingRepo.fetchLinkDetails(any()) } returns linkDetails

            val exception = shouldThrow<InvalidLinkException> { linkHandlingService.checkLinkValidity(uniqueLinkId) }

            exception.message shouldBe "Link has been expired."
        }

        "should check link validation after successfully hit once" {

            val linkDetails =
                LinkDetails(
                    linkId = uniqueLinkId,
                    // 21-04-2023 09:27:00 AM Approx
                    generationTime = Timestamp(System.currentTimeMillis()),
                    noOfHit = 1,
                    purposeOfLink = "Set Password",
                )

            every { linkHandlingRepo.fetchLinkDetails(any()) } returns linkDetails

            val exception = shouldThrow<InvalidLinkException> { linkHandlingService.checkLinkValidity(uniqueLinkId) }

            exception.message shouldBe "Set Password has been done once."
        }
    }
}
