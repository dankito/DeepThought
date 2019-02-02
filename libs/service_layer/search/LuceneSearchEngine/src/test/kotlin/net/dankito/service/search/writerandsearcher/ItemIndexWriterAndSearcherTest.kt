package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.extensions.previewWithSeriesAndPublishingDate
import net.dankito.service.search.FieldName
import net.dankito.service.search.LuceneSearchEngineIntegrationTestBase
import net.dankito.service.search.util.SortOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ItemIndexWriterAndSearcherTest : LuceneSearchEngineIntegrationTestBase() {


    @Test
    fun sourceTitleContains() {
        // given
        val source = createSource("Source Title", "27.03.2019")
        persist(source)

        val itemWithSource = Item("With source")
        itemWithSource.source = source
        persist(itemWithSource)

        val itemWithoutSource = Item("Without source")
        persist(itemWithoutSource)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("title")


        // then
        assertThat(result).hasSize(1)

        assertThat(result).containsExactly(itemWithSource)
    }

    @Test
    fun sourceSubTitleContains() {
        // given
        val source = createSource("Source Title", "27.03.2019")
        source.subTitle = "Source Sub Title"
        persist(source)

        val itemWithSource = Item("With source")
        itemWithSource.source = source
        persist(itemWithSource)

        val itemWithoutSource = Item("Without source")
        persist(itemWithoutSource)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("sub")


        // then
        assertThat(result).hasSize(1)

        assertThat(result).containsExactly(itemWithSource)
    }


    @Test
    fun sortByItemPreview_SortByContent_Ascending() {
        sortByItemPreview_SortByContent(true)
    }

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
    fun sortByItemPreview_SortByContent_CaseSensitive_Ascending() {
        sortByItemPreview_SortByContent_CaseSensitive(true)
    }

    @Test
    fun sortByItemPreview_SortByContent_CaseSensitive_Descending() {
        sortByItemPreview_SortByContent_CaseSensitive(false)
    }

    private fun sortByItemPreview_SortByContent_CaseSensitive(ascending: Boolean) {
        // given
        val itemWithSecondContent = Item("Queen")
        persist(itemWithSecondContent)

        val itemWithThirdContent = Item("the Strokes")
        persist(itemWithThirdContent)

        val itemWithFirstContent = Item("bloc Party")
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
    fun sortBySourcePreview_SortBySummary_Ascending() {
        sortBySourcePreview_SortBySummary(true)
    }

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
        val itemWithSecondIndication = Item("p. 16") // set content as equals() in assertThat() takes only content and summary into account
        itemWithSecondIndication.indication = "p. 16"
        persist(itemWithSecondIndication)

        val itemWithThirdIndication = Item("p. 111") // set content as equals() in assertThat() takes only content and summary into account
        itemWithThirdIndication.indication = "p. 111"
        persist(itemWithThirdIndication)

        val itemWithFirstIndication = Item("p. 5") // set content as equals() in assertThat() takes only content and summary into account
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

        val sourceWithSecondPublishingDate = createSource("", "27.03.2019", series)
        persist(sourceWithSecondPublishingDate)

        val itemWithSecondPublishingDate = Item(sourceWithSecondPublishingDate.previewWithSeriesAndPublishingDate) // to fix Item's equals() method in assert()
        itemWithSecondPublishingDate.source = sourceWithSecondPublishingDate
        persist(itemWithSecondPublishingDate)

        val sourceWithThirdPublishingDate = createSource("", "07.05.2019", series)
        persist(sourceWithThirdPublishingDate)

        val itemWithThirdPublishingDate = Item(sourceWithThirdPublishingDate.previewWithSeriesAndPublishingDate)
        itemWithThirdPublishingDate.source = sourceWithThirdPublishingDate
        persist(itemWithThirdPublishingDate)

        val sourceWithFirstPublishingDate = createSource("", "14.02.2019", series)
        persist(sourceWithFirstPublishingDate)

        val itemWithFirstPublishingDate = Item(sourceWithFirstPublishingDate.previewWithSeriesAndPublishingDate)
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

        val sourceWithSecondPublishingDateSecondTitle = createSource("Trump shocks the world", "27.03.2019", series)
        persist(sourceWithSecondPublishingDateSecondTitle)

        val itemWithSecondPublishingDateSecondTitle = Item(sourceWithSecondPublishingDateSecondTitle.previewWithSeriesAndPublishingDate) // to fix Item's equals() method in assert()
        itemWithSecondPublishingDateSecondTitle.source = sourceWithSecondPublishingDateSecondTitle
        persist(itemWithSecondPublishingDateSecondTitle)

        val sourceWithThirdPublishingDate = createSource("", "07.05.2019", series)
        persist(sourceWithThirdPublishingDate)

        val itemWithThirdPublishingDate = Item(sourceWithThirdPublishingDate.previewWithSeriesAndPublishingDate)
        itemWithThirdPublishingDate.source = sourceWithThirdPublishingDate
        persist(itemWithThirdPublishingDate)

        val sourceWithFirstPublishingDate = createSource("", "14.02.2019", series)
        persist(sourceWithFirstPublishingDate)

        val itemWithFirstPublishingDate = Item(sourceWithFirstPublishingDate.previewWithSeriesAndPublishingDate)
        itemWithFirstPublishingDate.source = sourceWithFirstPublishingDate
        persist(itemWithFirstPublishingDate)

        val sourceWithSecondPublishingDateFirstTitle = createSource("Obama rocks the world", "27.03.2019", series)
        persist(sourceWithSecondPublishingDateFirstTitle)

        val itemWithSecondPublishingDateFirstTitle = Item(sourceWithSecondPublishingDateFirstTitle.previewWithSeriesAndPublishingDate)
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


    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_Ascending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary(true)
    }

    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_Descending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary(false)
    }

    private fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary(ascending: Boolean) {
        // given
        val series = Series("New York Times")
        persist(series)

        val publishingDate = "27.03.2019"

        val sourceWithFirstTitle = createSource("Obama rocks the world", publishingDate, series)
        persist(sourceWithFirstTitle)

        val sourceWithSecondTitle = createSource("Trump shocks the world", publishingDate, series)
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
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_CaseSensitive_Ascending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_CaseSensitive(true)
    }

    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_CaseSensitive_Descending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_CaseSensitive(false)
    }

    private fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleAndSummary_CaseSensitive(ascending: Boolean) {
        // given
        val lowerCaseSeries = Series("c't")
        persist(lowerCaseSeries)
        val upperCaseSeries = Series("Heise")
        persist(upperCaseSeries)

        val publishingDate = "27.03.2019"

        val sourceWithFirstTitleUpperCaseSeries = createSource("millions of passwords leaked", publishingDate, upperCaseSeries)
        persist(sourceWithFirstTitleUpperCaseSeries)

        val sourceWithSecondTitleUpperCaseSeries = createSource("No passwords leaked", publishingDate, upperCaseSeries)
        persist(sourceWithSecondTitleUpperCaseSeries)

        val sourceWithFirstTitleLowerCaseSeries = createSource("millions of passwords leaked", publishingDate, lowerCaseSeries)
        persist(sourceWithFirstTitleLowerCaseSeries)

        val sourceWithSecondTitleLowerCaseSeries = createSource("No passwords leaked", publishingDate, lowerCaseSeries)
        persist(sourceWithSecondTitleLowerCaseSeries)


        val forthItem = Item(sourceWithSecondTitleUpperCaseSeries.previewWithSeriesAndPublishingDate) // to fix Item's equals() method in assert()
        forthItem.source = sourceWithSecondTitleUpperCaseSeries
        persist(forthItem)

        val secondItem = Item(sourceWithSecondTitleLowerCaseSeries.previewWithSeriesAndPublishingDate)
        secondItem.source = sourceWithSecondTitleLowerCaseSeries
        persist(secondItem)

        val firstItem = Item(sourceWithFirstTitleLowerCaseSeries.previewWithSeriesAndPublishingDate)
        firstItem.source = sourceWithFirstTitleLowerCaseSeries
        persist(firstItem)

        val thirdItem = Item(sourceWithFirstTitleUpperCaseSeries.previewWithSeriesAndPublishingDate)
        thirdItem.source = sourceWithFirstTitleUpperCaseSeries
        persist(thirdItem)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemSourcePreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(4)

        if(ascending) {
            assertThat(result).containsExactly(firstItem, secondItem, thirdItem, forthItem)
        }
        else {
            assertThat(result).containsExactly(forthItem, thirdItem, secondItem, firstItem)
        }
    }


    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleSummaryAndIndication_CaseSensitive_Ascending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleSummaryAndIndication_CaseSensitive(true)
    }

    @Test
    fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleSummaryAndIndication_CaseSensitive_Descending() {
        sortBySourcePreview_SortBySeriesPublishingDateSourceTitleSummaryAndIndication_CaseSensitive(false)
    }

    private fun sortBySourcePreview_SortBySeriesPublishingDateSourceTitleSummaryAndIndication_CaseSensitive(ascending: Boolean) {
        // given
        val lowerCaseSeries = Series("c't")
        persist(lowerCaseSeries)
        val upperCaseSeries = Series("Heise")
        persist(upperCaseSeries)

        val publishingDate = "27.03.2019"

        val sourceWithFirstTitleUpperCaseSeries = createSource("millions of passwords leaked", publishingDate, upperCaseSeries)
        persist(sourceWithFirstTitleUpperCaseSeries)

        val sourceWithSecondTitleUpperCaseSeries = createSource("No passwords leaked", publishingDate, upperCaseSeries)
        persist(sourceWithSecondTitleUpperCaseSeries)

        val sourceWithFirstTitleLowerCaseSeries = createSource("millions of passwords leaked", publishingDate, lowerCaseSeries)
        persist(sourceWithFirstTitleLowerCaseSeries)

        val sourceWithSecondTitleLowerCaseSeries = createSource("No passwords leaked", publishingDate, lowerCaseSeries)
        persist(sourceWithSecondTitleLowerCaseSeries)


        val sixthItem = Item(sourceWithFirstTitleUpperCaseSeries.previewWithSeriesAndPublishingDate + "p. 88") // to fix Item's equals() method in assert()
        sixthItem.indication = "p. 88"
        sixthItem.source = sourceWithFirstTitleUpperCaseSeries
        persist(sixthItem)

        val secondItem = Item(sourceWithFirstTitleLowerCaseSeries.previewWithSeriesAndPublishingDate + "p. 21")
        secondItem.indication = "p. 21"
        secondItem.source = sourceWithFirstTitleLowerCaseSeries
        persist(secondItem)

        val eighthItem = Item(sourceWithSecondTitleUpperCaseSeries.previewWithSeriesAndPublishingDate + "p. 2")
        eighthItem.indication = "p. 2"
        eighthItem.source = sourceWithSecondTitleUpperCaseSeries
        persist(eighthItem)

        val fourthItem = Item(sourceWithSecondTitleLowerCaseSeries.previewWithSeriesAndPublishingDate + "p. 56")
        fourthItem.indication = "p. 56"
        fourthItem.source = sourceWithSecondTitleLowerCaseSeries
        persist(fourthItem)

        val firstItem = Item(sourceWithFirstTitleLowerCaseSeries.previewWithSeriesAndPublishingDate + "p. 20")
        firstItem.indication = "p. 20"
        firstItem.source = sourceWithFirstTitleLowerCaseSeries
        persist(firstItem)

        val fifthItem = Item(sourceWithFirstTitleUpperCaseSeries.previewWithSeriesAndPublishingDate + "p. 87")
        fifthItem.indication = "p. 87"
        fifthItem.source = sourceWithFirstTitleUpperCaseSeries
        persist(fifthItem)

        val thirdItem = Item(sourceWithSecondTitleLowerCaseSeries.previewWithSeriesAndPublishingDate + "p. 55")
        thirdItem.indication = "p. 55"
        thirdItem.source = sourceWithSecondTitleLowerCaseSeries
        persist(thirdItem)

        val seventhItem = Item(sourceWithSecondTitleUpperCaseSeries.previewWithSeriesAndPublishingDate + "p. 1")
        seventhItem.indication = "p. 1"
        seventhItem.source = sourceWithSecondTitleUpperCaseSeries
        persist(seventhItem)

        waitTillEntityGetsIndexed()


        // when
        val result = searchItems("", sortOptions = listOf(SortOption(FieldName.ItemSourcePreviewForSorting, ascending)))


        // then
        assertThat(result).hasSize(8)

        if(ascending) {
            assertThat(result).containsExactly(firstItem, secondItem, thirdItem, fourthItem, fifthItem, sixthItem, seventhItem, eighthItem)
        }
        else {
            assertThat(result).containsExactly(eighthItem, seventhItem, sixthItem, fifthItem, fourthItem, thirdItem, secondItem, firstItem)
        }
    }

}