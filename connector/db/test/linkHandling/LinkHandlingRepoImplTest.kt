package linkHandling

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import scalereal.core.linkHandling.LinkDetails
import scalereal.db.linkHandling.LinkHandlingRepoImpl
import util.StringSpecWithDataSource
import java.sql.Timestamp

class LinkHandlingRepoImplTest : StringSpecWithDataSource() {
    private lateinit var linkHandlingRepoImpl: LinkHandlingRepoImpl

    init {

        val uniqueLinkId = "unique_link_id"
        "should add new link details" {
            val linkDetails =
                LinkDetails(
                    linkId = uniqueLinkId,
                    // 21-04-2023 09:27:00 AM Approx
                    generationTime = Timestamp(1682049422061),
                    noOfHit = 0,
                    purposeOfLink = "Set Password",
                )

            linkHandlingRepoImpl.insertLinkDetails(linkDetails)
        }

        "should get link details by link Id " {

            val linkDetail = linkHandlingRepoImpl.fetchLinkDetails(uniqueLinkId)

            linkDetail.generationTime shouldBe Timestamp(1682049422061)
            linkDetail.noOfHit shouldBe 0
            linkDetail.purposeOfLink shouldBe "Set Password"
        }

        "should able to update link details" {

            val linkDetails = linkHandlingRepoImpl.updateLinkDetails(1, uniqueLinkId)

            linkDetails.linkId shouldBe uniqueLinkId
            linkDetails.noOfHit shouldBe 1
            linkDetails.generationTime shouldBe Timestamp(1682049422061)
            linkDetails.purposeOfLink shouldBe "Set Password"
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        linkHandlingRepoImpl = LinkHandlingRepoImpl(dataSource)
    }
}
