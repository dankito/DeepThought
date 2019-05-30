package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Series
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedSeriesSerializerTest : PersistedEntitySerializerTestBase<Series>(Series::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Series> {
        return PersistedSeriesSerializer(mock(StdSerializer::class.java) as StdSerializer<Series>)
    }

    override fun createEntity(): Series {
        return Series("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.SeriesIdFieldName
    }

}