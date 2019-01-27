package net.dankito.service.search

import net.dankito.deepthought.model.Tag
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class CorrectStringFieldComparatorTest : LuceneSearchEngineIntegrationTestBase() {

    /**
     * Lucene isn't aware of German Umlaute when sorting results.
     *
     * 'Ä', 'Ö' and 'Ü' get ranked after 'Z' but should get ranked after 'A', 'O' or 'U' respectively.
     */
    @Test
    fun germanUmlaute() {
        // given
        val tag2 = Tag("Äcker")
        persist(tag2)

        val tag5 = Tag("Uber")
        persist(tag5)

        val tag7 = Tag("Zucker")
        persist(tag7)

        val tag1 = Tag("Acker")
        persist(tag1)

        val tag6 = Tag("Über")
        persist(tag6)

        val tag4 = Tag("Olfaktorisch")
        persist(tag4)

        val tag3 = Tag("Öl")
        persist(tag3)

        waitTillEntityGetsIndexed()


        // when
        val result = searchTags().getRelevantMatchesSorted()


        // then
        assertThat(result).containsExactly(tag1, tag2, tag3, tag4, tag5, tag6, tag7)
    }


    /**
     * Also it has problems with mixed upper and lower case words.
     */
    @Test
    fun germanUmlaute_MixedCase() {
        // given
        val tag2 = Tag("Äcker")
        persist(tag2)

        val tag5 = Tag("uber")
        persist(tag5)

        val tag7 = Tag("Zucker")
        persist(tag7)

        val tag1 = Tag("acker")
        persist(tag1)

        val tag6 = Tag("Über")
        persist(tag6)

        val tag4 = Tag("Olfaktorisch")
        persist(tag4)

        val tag3 = Tag("öl")
        persist(tag3)

        waitTillEntityGetsIndexed()


        // when
        val result = searchTags().getRelevantMatchesSorted()


        // then
        assertThat(result).containsExactly(tag1, tag2, tag3, tag4, tag5, tag6, tag7)
    }

}