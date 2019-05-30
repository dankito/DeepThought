package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Series
import net.dankito.service.data.SeriesService
import net.dankito.utils.serialization.SerializerConfig


class PersistedSeriesDeserializer(seriesService: SeriesService, defaultDeserializer: StdDeserializer<Series>)
    : PersistedEntityDeserializerBase<Series>(seriesService, defaultDeserializer, SerializerConfig.SeriesIdFieldName, Series::class.java)