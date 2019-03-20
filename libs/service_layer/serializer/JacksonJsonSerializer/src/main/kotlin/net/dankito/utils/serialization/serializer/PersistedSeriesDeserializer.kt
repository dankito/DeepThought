package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Series
import net.dankito.service.data.SeriesService


class PersistedSeriesDeserializer(seriesService: SeriesService)
    : PersistedEntityDeserializerBase<Series>(seriesService, SerializerConfig.SeriesIdFieldName, Series::class.java)