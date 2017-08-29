package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.TagService
import org.slf4j.LoggerFactory
import java.io.IOException


class PersistedTagDeserializer(private val tagService: TagService) : StdDeserializer<Tag>(Tag::class.java) {

    companion object {
        private val log = LoggerFactory.getLogger(PersistedTagDeserializer::class.java)
    }


    @Throws(JsonProcessingException::class, IOException::class)
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Tag? {
        p?.let { jsonParser ->
            try {
                val node = jsonParser.codec.readTree<ObjectNode>(jsonParser)

                val tagIdNode = node.get(SerializerConfig.TagIdFieldName)

                if(tagIdNode != null) {
                    val tagId = tagIdNode.asText()

                    return tagService.retrieve(tagId)
                }

                return null
            } catch(e: Exception) {
                log.info("Could not deserialize Tag", e)
                return null
            }
        }

        throw IOException("Could not deserialize Tag, JsonParser is null")
    }

}