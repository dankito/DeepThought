package net.dankito.deepthought.model.extensions

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ItemExtensionsTest {

    companion object {
        private const val SourceTitle = "Title"
        private const val SourceSubTitle = "SubTitle"

        private const val Indication = "p. 117"
    }


    @Test
    fun sourcePreview_SourceWithoutSubTitle_NoIndication() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle)

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceTitle)
    }

    @Test
    fun sourcePreview_SourceWithSubTitle_NoIndication() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle, "", subTitle = SourceSubTitle)

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceSubTitle + ": " + SourceTitle)
    }


    @Test
    fun sourcePreview_SourceWithoutSubTitle_IndicationSet() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle)
        item.indication = Indication

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceTitle + " " + Indication)
    }

    @Test
    fun sourcePreview_SourceWithSubTitle_IndicationSet() {
        // given
        val item = Item("")
        item.source = Source(SourceTitle, "", subTitle = SourceSubTitle)
        item.indication = Indication

        // when
        val result = item.sourcePreview

        // then
        assertThat(result).isEqualTo(SourceSubTitle + ": " + SourceTitle + " " + Indication)
    }

}