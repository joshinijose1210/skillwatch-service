package scalereal.core.models.domain

data class KRAData(
    val srNo: Long,
    var name: String,
    var weightage: Int,
    val versionNumber: Int,
    val organisationId: Long,
)

data class GetAllKRAResponse(
    val id: Long,
    val kraId: String,
    var name: String,
    var weightage: Int,
    val organisationId: Long,
) {
    companion object {
        fun getKRADisplayId(kraSrNo: Long) = "KRA${kraSrNo.toString().padStart(2, '0')}"
    }
}

data class KRAWeightage(
    val id: Long,
    val weightage: Int,
)
