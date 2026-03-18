package io.htmltopdf

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@Tags("walking-skeleton")
class WalkingSkeletonSpec : BehaviorSpec({

    given("a plain HTML string with no template variables") {
        val plainHtml = "<html><body><h1>Invoice #1001</h1><p>Total: \$240.00</p></body></html>"

        `when`("htmlToPdf is called with no data") {
            val result = htmlToPdf(plainHtml)

            then("returns a non-null InputStream") {
                result shouldNotBe null
            }

            then("stream begins with PDF magic bytes %PDF") {
                val magicBytes = result.readNBytes(4)
                magicBytes shouldBe "%PDF".toByteArray()
            }
        }
    }
})
