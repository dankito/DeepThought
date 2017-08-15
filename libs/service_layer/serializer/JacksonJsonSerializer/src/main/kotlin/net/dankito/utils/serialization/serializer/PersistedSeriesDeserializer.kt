package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import net.dankito.deepthought.model.Series
import net.dankito.service.data.SeriesService
import java.io.IOException


class PersistedSeriesDeserializer(private val seriesService: SeriesService) : StdDeserializer<Series>(Series::class.java) {

    @Throws(JsonProcessingException::class, IOException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Series? {
        p?.let { jsonParser ->
            try {
                val node = jsonParser.codec.readTree<ObjectNode>(jsonParser)

                val seriesIdNode = node.get(SerializerConfig.SeriesIdFieldName)

                if(seriesIdNode != null) {
                    val seriesId = seriesIdNode.asText()

                    return seriesService.retrieve(seriesId)
                }

                return null
            } catch(e: Exception) {
                throw IOException("Could not deserialize Series", e)
            }
        }

        throw IOException("Could not deserialize Series, JsonParser is null")
    }

}