package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.data.EntityServiceBase
import org.slf4j.LoggerFactory
import java.io.IOException


open class PersistedEntityDeserializerBase<TEntity : BaseEntity>(protected val entityService: EntityServiceBase<TEntity>, protected val idFieldName: String,
                                                                 protected val entityClass: Class<TEntity>) : StdDeserializer<TEntity>(entityClass) {

    companion object {
        private val log = LoggerFactory.getLogger(PersistedEntityDeserializerBase::class.java)
    }


    @Throws(JsonProcessingException::class, IOException::class)
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): TEntity? {
        parser?.let { jsonParser ->
            try {
                val node = jsonParser.codec.readTree<ObjectNode>(jsonParser)

                val idNode = node.get(idFieldName)

                if(idNode != null) {
                    val entityId = idNode.asText()

                    return entityService.retrieve(entityId)
                }

                return null
            } catch(e: Exception) {
                log.error("Could not deserialize ${entityClass.simpleName}", e)
                return null
            }
        }

        throw IOException("Could not deserialize ${entityClass.simpleName}, JsonParser is null")
    }

}