package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Series
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.SeriesService
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedSeriesDeserializerTest : PersistedEntityDeserializerTestBase<Series>(Series::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<Series> {
        return mock(SeriesService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<Series>): PersistedEntityDeserializerBase<Series> {
        return PersistedSeriesDeserializer(entityService as SeriesService, mock(StdDeserializer::class.java) as StdDeserializer<Series>)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.SeriesIdFieldName
    }

}