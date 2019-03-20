package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Series


class PersistedSeriesSerializer : PersistedEntitySerializerBase<Series>(SerializerConfig.SeriesIdFieldName, Series::class.java)