package net.dankito.utils.serialization.deserializer

import net.dankito.deepthought.model.Series
import net.dankito.service.data.SeriesService
import net.dankito.utils.serialization.SerializerConfig


class PersistedSeriesDeserializer(seriesService: SeriesService)
    : PersistedEntityDeserializerBase<Series>(seriesService, SerializerConfig.SeriesIdFieldName, Series::class.java)