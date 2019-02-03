package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Tag
import net.dankito.service.search.LuceneSearchEngineIntegrationTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class TagIndexWriterAndSearcherTest : LuceneSearchEngineIntegrationTestBase() {


    @Test
    fun searchTags_TagWithSpace() {
        // given
        val tagWithSpace = Tag("Mahatma Gandhi")
        persistTag(tagWithSpace)

        // when
        val result = searchTags(tagWithSpace.name)

        // then
        assertThat(result.results).hasSize(1)
        assertThat(result.results[0].hasExactMatches()).isTrue()
        assertThat(result.results[0].exactMatches).hasSize(1)
        assertThat(result.results[0].exactMatches[0]).isEqualTo(tagWithSpace)
    }

    @Test
    fun searchTags_TagWithDash() {
        // given
        val tagWithDash = Tag("Hans-Georg")
        persistTag(tagWithDash)

        // when
        val result = searchTags(tagWithDash.name)

        // then
        assertThat(result.results).hasSize(1)
        assertThat(result.results[0].hasExactMatches()).isTrue()
        assertThat(result.results[0].exactMatches).hasSize(1)
        assertThat(result.results[0].exactMatches[0]).isEqualTo(tagWithDash)
    }

    @Test
    fun searchTags_TestSortOrder() {
        // given
        persist(Tag("A"))
        persist(Tag("B"))
        persist(Tag("c"))
        persist(Tag("D"))
        persist(Tag("E"))
        persist(Tag("F"))
        persist(Tag("G"))
        persist(Tag("h"))
        persist(Tag("o"))
        persist(Tag("U"))
        persist(Tag("Z"))
        persist(Tag("Ärger"))
        persist(Tag("Armutszeugnis"))
        persist(Tag("Verschlüsselung"))
        persist(Tag("Bild"))
        persist(Tag("bundesregierung"))
        persist(Tag("Heise"))
        persist(Tag("Öl"))
        persist(Tag("Polizei"))
        persist(Tag("Verschlusselung"))
        persist(Tag("Überwachungsstaat"))

        waitTillEntityGetsIndexed()
        waitTillEntityGetsIndexed()


        // when
        val result = searchTags()

        // then
        assertThat(result.results).hasSize(1)
//        assertThat(result.results[0].allMatchesCount).isEqualTo(21) // TODO
        assertThat(result.results[0].hasExactMatches()).isFalse()

        assertThat(result.getRelevantMatchesSorted()).extracting("name").containsExactly(
                "A", "Ärger", "Armutszeugnis",
                "B", "Bild", "bundesregierung",
                "c",
                "D",
                "E",
                "F",
                "G",
                "h", "Heise",
                "o", "Öl",
                "Polizei",
                "U", "Überwachungsstaat",
                "Verschlusselung", "Verschlüsselung",
                "Z"
        )
    }

    private fun persistTag(tag: Tag, countDummyTags: Int = 3) {
        persist(tag)

        for(i in 0 until countDummyTags) {
            persist(Tag("$i"))
        }

        waitTillEntityGetsIndexed()
    }
}