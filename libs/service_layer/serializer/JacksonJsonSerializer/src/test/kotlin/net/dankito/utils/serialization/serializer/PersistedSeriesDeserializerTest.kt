package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Series
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.SeriesService
import org.mockito.Mockito.mock


class PersistedSeriesDeserializerTest : PersistedEntityDeserializerTestBase<Series>(Series::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<Series> {
        return mock(SeriesService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<Series>): PersistedEntityDeserializerBase<Series> {
        return PersistedSeriesDeserializer(entityService as SeriesService)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.SeriesIdFieldName
    }

}