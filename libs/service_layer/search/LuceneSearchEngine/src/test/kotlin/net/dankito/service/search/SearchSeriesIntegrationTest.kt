package net.dankito.service.search

import net.dankito.deepthought.model.Series
import net.dankito.service.search.specific.SeriesSearch
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


class SearchSeriesIntegrationTest: LuceneSearchEngineIntegrationTestBase() {

    companion object {
        private const val TestSeriesTitle = "Love"
    }


    @Test
    fun noSeriesOfThatNameExists() {
        executeTest(0)
    }

    @Test
    fun create5SeriesStartingWithThatName() {
        executeTest(5)
    }


    private fun executeTest(countSeries: Int) {
        createCountSeriesStartingWithTitle(countSeries)


        val resultHolder = AtomicReference<List<Series>>()
        executeSearch(resultHolder)


        testResult(resultHolder, countSeries)
    }


    private fun executeSearch(resultHolder: AtomicReference<List<Series>>, searchTerm: String = TestSeriesTitle) {
        val countDownLatch = CountDownLatch(1)

        underTest.searchSeries(SeriesSearch(searchTerm) {
            resultHolder.set(it)
            countDownLatch.countDown()
        })

        countDownLatch.await()
    }

    private fun testResult(resultHolder: AtomicReference<List<Series>>, countSeries: Int, seriesTitlePrefix: String = TestSeriesTitle) {
        val result = resultHolder.get()

        Assert.assertThat(result, CoreMatchers.notNullValue())
        Assert.assertThat(result.size, CoreMatchers.`is`(countSeries))

        result.forEach {
            Assert.assertThat(it.title.startsWith(seriesTitlePrefix), CoreMatchers.`is`(true))
        }
    }


    private fun createCountSeriesStartingWithTitle(countSeries: Int, seriesTitlePrefix: String = TestSeriesTitle) {
        for(i in 1..countSeries) {
            val series = Series(seriesTitlePrefix + i)
            seriesService.persist(series)
        }
    }

}