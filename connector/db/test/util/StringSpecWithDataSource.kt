package util

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec

open class StringSpecWithDataSource : StringSpec() {
    open lateinit var dataSource: SpecDataSource

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource = SpecDataSource()
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        dataSource.cleanup()
    }
}
