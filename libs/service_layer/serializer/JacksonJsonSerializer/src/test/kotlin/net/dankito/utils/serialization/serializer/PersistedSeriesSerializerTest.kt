package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Series
import net.dankito.utils.serialization.SerializerConfig


class PersistedSeriesSerializerTest : PersistedEntitySerializerTestBase<Series>(Series::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Series> {
        return PersistedSeriesSerializer()
    }

    override fun createEntity(): Series {
        return Series("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.SeriesIdFieldName
    }

}