package net.dankito.deepthought.utils

import com.nhaarman.mockito_kotlin.doReturn
import net.dankito.deepthought.model.*
import net.dankito.service.data.*
import net.dankito.service.search.FieldName.FileId
import net.dankito.service.search.FieldName.ItemId
import net.dankito.service.search.FieldName.SeriesId
import net.dankito.service.search.FieldName.SourceId
import net.dankito.service.search.FieldName.TagId
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock

class DeepThoughtJacksonJsonSerializerTest {

    companion object {
        private const val Id = "id"

        private const val ItemContent = "item_content"

        private const val TagName = "tag_name"

        private const val SourceTitle = "source_title"

        private const val SeriesTitle = "series_title"

        private const val FileUri = "uri://path"
    }


    private val itemService = mock(ItemService::class.java)

    private val tagService = mock(TagService::class.java)

    private val sourceService = mock(SourceService::class.java)

    private val seriesService = mock(SeriesService::class.java)

    private val fileService = mock(FileService::class.java)


    private val underTest = DeepThoughtJacksonJsonSerializer(itemService, tagService, sourceService, seriesService, fileService)


    @Test
    fun serializeUnpersistedItem() {

        // given
        val unpersistedItem = Item(ItemContent)


        // when
        val result = underTest.serializeObject(unpersistedItem)


        // then
        assertThat(result).doesNotContain("\"id\":")
        assertThat(result).doesNotContain("\"$ItemId\":")
        assertThat(result).contains(ItemContent)
    }

    @Test
    fun serializePersistedItem() {

        // given
        val persistedItem = Item(ItemContent)
        persistedItem.id = Id


        // when
        val result = underTest.serializeObject(persistedItem)


        // then
        assertThat(result).contains("\"$ItemId\":\"$Id\"")
        assertThat(result).doesNotContain(ItemContent)
    }

    @Test
    fun deserializeUnpersistedItem() {

        // when
        val result = underTest.deserializeObject("{\"content\":\"$ItemContent\"}", Item::class.java)


        // then
        assertThat(result).isNotNull
        assertThat(result.content).isEqualTo(ItemContent)
    }

    @Test
    fun deserializePersistedItem() {

        // given
        val persistedItem = Item(ItemContent)
        persistedItem.id = Id

        doReturn(persistedItem).`when`(itemService).retrieve(Id)


        // when
        val result = underTest.deserializeObject("{\"$ItemId\":\"$Id\"}", Item::class.java)


        // then
        assertThat(result).isEqualTo(persistedItem)
    }


    @Test
    fun serializeUnpersistedTag() {

        // given
        val unpersistedTag = Tag(TagName)


        // when
        val result = underTest.serializeObject(unpersistedTag)


        // then
        assertThat(result).doesNotContain("\"id\":")
        assertThat(result).doesNotContain("\"$TagId\":")
        assertThat(result).contains(TagName)
    }

    @Test
    fun serializePersistedTag() {

        // given
        val persistedTag = Tag(TagName)
        persistedTag.id = Id


        // when
        val result = underTest.serializeObject(persistedTag)


        // then
        assertThat(result).contains("\"$TagId\":\"$Id\"")
        assertThat(result).doesNotContain(TagName)
    }


    @Test
    fun serializeUnpersistedSource() {

        // given
        val unpersistedSource = Source(SourceTitle)


        // when
        val result = underTest.serializeObject(unpersistedSource)


        // then
        assertThat(result).doesNotContain("\"id\":")
        assertThat(result).doesNotContain("\"$SourceId\":")
        assertThat(result).contains(SourceTitle)
    }

    @Test
    fun serializePersistedSource() {

        // given
        val persistedSource = Source(SourceTitle)
        persistedSource.id = Id


        // when
        val result = underTest.serializeObject(persistedSource)


        // then
        assertThat(result).contains("\"$SourceId\":\"$Id\"")
        assertThat(result).doesNotContain(SourceTitle)
    }


    @Test
    fun serializeUnpersistedSeries() {

        // given
        val unpersistedSeries = Series(SeriesTitle)


        // when
        val result = underTest.serializeObject(unpersistedSeries)


        // then
        assertThat(result).doesNotContain("\"id\":")
        assertThat(result).doesNotContain("\"$SeriesId\":")
        assertThat(result).contains(SeriesTitle)
    }

    @Test
    fun serializePersistedSeries() {

        // given
        val persistedSeries = Series(SeriesTitle)
        persistedSeries.id = Id


        // when
        val result = underTest.serializeObject(persistedSeries)


        // then
        assertThat(result).contains("\"$SeriesId\":\"$Id\"")
        assertThat(result).doesNotContain(SeriesTitle)
    }


    @Test
    fun serializeUnpersistedFile() {

        // given
        val unpersistedFile = FileLink(TagName)


        // when
        val result = underTest.serializeObject(unpersistedFile)


        // then
        assertThat(result).doesNotContain("\"id\":")
        assertThat(result).doesNotContain("\"$FileId\":")
        assertThat(result).contains(TagName)
    }

    @Test
    fun serializePersistedFile() {

        // given
        val persistedFile = FileLink(FileUri)
        persistedFile.id = Id


        // when
        val result = underTest.serializeObject(persistedFile)


        // then
        assertThat(result).contains("\"$FileId\":\"$Id\"")
        assertThat(result).doesNotContain(FileUri)
    }

}