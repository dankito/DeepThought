package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Series
import net.dankito.utils.serialization.SerializerConfig


class PersistedSeriesSerializer(defaultSerializer: StdSerializer<Series>)
    : PersistedEntitySerializerBase<Series>(defaultSerializer, SerializerConfig.SeriesIdFieldName, Series::class.java)