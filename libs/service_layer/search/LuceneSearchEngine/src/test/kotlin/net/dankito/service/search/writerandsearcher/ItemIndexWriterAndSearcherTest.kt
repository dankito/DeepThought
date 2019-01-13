package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.service.search.FieldName
import net.dankito.service.search.LuceneSearchEngineIntegrationTestBase
import net.dankito.service.search.util.SortOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ItemIndexWriterAndSearcherTest : LuceneSearchEngineIntegrationTestBase() {


    @Test
    fun sortByItemPreview_SortByContent_Ascending() {
        sortByItemPreview_SortByContent(true)
    }

    // FIXME: CorrectStringComparatorSource doesn't work in this case, test therefore fails
    @Test
    fun sortByItemPreview_SortByContent_Descending() {
        sortByItemPreview_SortByContent(false)
    }

    private fun sortByItemPreview_SortByContent(ascending: Boolean) {
        // given
        val itemWithSecondContent = Item("Queen")
        persist(itemWithSecondContent)

        val itemWithThirdContent = Item("The Strokes")
        persist(itemWithThirdContent)

        val itemWithFirstContent = Item("Bloc Party")
        persist(itemWithFirstContent)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemPreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(3)

        if(ascending) {
            assertThat(result).containsExactly(itemWithFirstContent, itemWithSecondContent, itemWithThirdContent)
        }
        else {
            assertThat(result).containsExactly(itemWithThirdContent, itemWithSecondContent, itemWithFirstContent)
        }
    }


    @Test
    fun sortBySourcePreview_SortBySeriesAndPublishingDate_Ascending() {
        sortBySourcePreview_SortBySeriesAndPublishingDate(true)
    }

    @Test
    fun sortBySourcePreview_SortBySeriesAndPublishingDate_Descending() {
        sortBySourcePreview_SortBySeriesAndPublishingDate(false)
    }

    private fun sortBySourcePreview_SortBySeriesAndPublishingDate(ascending: Boolean) {
        // given
        val series = Series("New York Times")
        persist(series)

        val sourceWithSecondPublishingDate = createSource("", "27.03.19", series)
        persist(sourceWithSecondPublishingDate)

        val itemWithSecondPublishingDate = Item("")
        itemWithSecondPublishingDate.source = sourceWithSecondPublishingDate
        persist(itemWithSecondPublishingDate)

        val sourceWithThirdPublishingDate = createSource("", "07.05.19", series)
        persist(sourceWithThirdPublishingDate)

        val itemWithThirdPublishingDate = Item("")
        itemWithThirdPublishingDate.source = sourceWithThirdPublishingDate
        persist(itemWithThirdPublishingDate)

        val sourceWithFirstPublishingDate = createSource("", "14.02.19", series)
        persist(sourceWithFirstPublishingDate)

        val itemWithFirstPublishingDate = Item("")
        itemWithFirstPublishingDate.source = sourceWithFirstPublishingDate
        persist(itemWithFirstPublishingDate)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemSourcePreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(3)

        if(ascending) {
            assertThat(result).containsExactly(itemWithFirstPublishingDate, itemWithSecondPublishingDate, itemWithThirdPublishingDate)
        }
        else {
            assertThat(result).containsExactly(itemWithThirdPublishingDate, itemWithSecondPublishingDate, itemWithFirstPublishingDate)
        }
    }


    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateAndSourceTitle_Ascending() {
        sortBySourcePreview_SortBySeriesPublishingDateAndSourceTitle(true)
    }

    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateAndSourceTitle_Descending() {
        sortBySourcePreview_SortBySeriesPublishingDateAndSourceTitle(false)
    }

    private fun sortBySourcePreview_SortBySeriesPublishingDateAndSourceTitle(ascending: Boolean) {
        // given
        val series = Series("New York Times")
        persist(series)

        val sourceWithSecondPublishingDateSecondTitle = createSource("Trump shocks the world", "27.03.19", series)
        persist(sourceWithSecondPublishingDateSecondTitle)

        val itemWithSecondPublishingDateSecondTitle = Item("")
        itemWithSecondPublishingDateSecondTitle.source = sourceWithSecondPublishingDateSecondTitle
        persist(itemWithSecondPublishingDateSecondTitle)

        val sourceWithThirdPublishingDate = createSource("", "07.05.19", series)
        persist(sourceWithThirdPublishingDate)

        val itemWithThirdPublishingDate = Item("")
        itemWithThirdPublishingDate.source = sourceWithThirdPublishingDate
        persist(itemWithThirdPublishingDate)

        val sourceWithFirstPublishingDate = createSource("", "14.02.19", series)
        persist(sourceWithFirstPublishingDate)

        val itemWithFirstPublishingDate = Item("")
        itemWithFirstPublishingDate.source = sourceWithFirstPublishingDate
        persist(itemWithFirstPublishingDate)

        val sourceWithSecondPublishingDateFirstTitle = createSource("Obama rocks the world", "27.03.19", series)
        persist(sourceWithSecondPublishingDateFirstTitle)

        val itemWithSecondPublishingDateFirstTitle = Item("")
        itemWithSecondPublishingDateFirstTitle.source = sourceWithSecondPublishingDateFirstTitle
        persist(itemWithSecondPublishingDateFirstTitle)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemSourcePreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(4)

        if(ascending) {
            assertThat(result).containsExactly(itemWithFirstPublishingDate, itemWithSecondPublishingDateFirstTitle, itemWithSecondPublishingDateSecondTitle, itemWithThirdPublishingDate)
        }
        else {
            assertThat(result).containsExactly(itemWithThirdPublishingDate, itemWithSecondPublishingDateSecondTitle, itemWithSecondPublishingDateFirstTitle, itemWithFirstPublishingDate)
        }
    }


    // FIXME: CorrectStringComparatorSource doesn't work in this case, test therefore fails
    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_Ascending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary(true)
    }

    // FIXME: CorrectStringComparatorSource doesn't work in this case, test therefore fails
    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_Descending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary(false)
    }

    private fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary(ascending: Boolean) {
        // given
        val series = Series("New York Times")
        persist(series)

        val sourceWithFirstTitle = createSource("Obama rocks the world", "27.03.19", series)
        persist(sourceWithFirstTitle)

        val sourceWithSecondTitle = createSource("Trump shocks the world", "27.03.19", series)
        persist(sourceWithSecondTitle)


        val itemWithFirstTitleSecondSummary = Item("", "Obama")
        itemWithFirstTitleSecondSummary.source = sourceWithFirstTitle
        persist(itemWithFirstTitleSecondSummary)

        val itemWithSecondTitleSecondSummary = Item("", "Trump")
        itemWithSecondTitleSecondSummary.source = sourceWithSecondTitle
        persist(itemWithSecondTitleSecondSummary)

        val itemWithSecondTitleFirstSummary = Item("", "Bump")
        itemWithSecondTitleFirstSummary.source = sourceWithSecondTitle
        persist(itemWithSecondTitleFirstSummary)

        val itemWithFirstTitleFirstSummary = Item("", "Michelle")
        itemWithFirstTitleFirstSummary.source = sourceWithFirstTitle
        persist(itemWithFirstTitleFirstSummary)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemSourcePreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(4)

        if(ascending) {
            assertThat(result).containsExactly(itemWithFirstTitleFirstSummary, itemWithFirstTitleSecondSummary, itemWithSecondTitleFirstSummary, itemWithSecondTitleSecondSummary)
        }
        else {
            assertThat(result).containsExactly(itemWithSecondTitleSecondSummary, itemWithSecondTitleFirstSummary, itemWithFirstTitleSecondSummary, itemWithFirstTitleFirstSummary)
        }
    }


    @Test
    fun sortBySourcePreview_SortBySummary_Ascending() {
        sortBySourcePreview_SortBySummary(true)
    }

    // FIXME: CorrectStringComparatorSource doesn't work in this case, test therefore fails
    @Test
    fun sortBySourcePreview_SortBySummary_Descending() {
        sortBySourcePreview_SortBySummary(false)
    }

    private fun sortBySourcePreview_SortBySummary(ascending: Boolean) {
        // given
        val itemWithSecondSummary = Item("", "Queen")
        persist(itemWithSecondSummary)

        val itemWithThirdSummary = Item("", "The Strokes")
        persist(itemWithThirdSummary)

        val itemWithFirstSummary = Item("", "Bloc Party")
        persist(itemWithFirstSummary)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemSourcePreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(3)

        if(ascending) {
            assertThat(result).containsExactly(itemWithFirstSummary, itemWithSecondSummary, itemWithThirdSummary)
        }
        else {
            assertThat(result).containsExactly(itemWithThirdSummary, itemWithSecondSummary, itemWithFirstSummary)
        }
    }


    @Test
    fun sortBySourcePreview_SortByIndication_Ascending() {
        sortBySourcePreview_SortByIndication(true)
    }

    @Test
    fun sortBySourcePreview_SortByIndication_Descending() {
        sortBySourcePreview_SortByIndication(false)
    }

    private fun sortBySourcePreview_SortByIndication(ascending: Boolean) {
        // given
        val itemWithSecondIndication = Item("")
        itemWithSecondIndication.indication = "p. 16"
        persist(itemWithSecondIndication)

        val itemWithThirdIndication = Item("")
        itemWithThirdIndication.indication = "p. 111"
        persist(itemWithThirdIndication)

        val itemWithFirstIndication = Item("")
        itemWithFirstIndication.indication = "p. 5"
        persist(itemWithFirstIndication)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemSourcePreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(3)

        // the problem is indication is a String and therefore sorted alphanumerically. The correct sort order would be reverse.
        if(ascending) {
            assertThat(result).containsExactly(itemWithThirdIndication, itemWithSecondIndication, itemWithFirstIndication)
        }
        else {
            assertThat(result).containsExactly(itemWithFirstIndication, itemWithSecondIndication, itemWithThirdIndication)
        }
    }

}