package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Series
import net.dankito.utils.serialization.SerializerConfig


class PersistedSeriesSerializer : PersistedEntitySerializerBase<Series>(SerializerConfig.SeriesIdFieldName, Series::class.java)