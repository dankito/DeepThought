package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.data.EntityServiceBase
import org.slf4j.LoggerFactory
import java.io.IOException


open class PersistedEntityDeserializerBase<TEntity : BaseEntity>(protected val entityService: EntityServiceBase<TEntity>,
                                                                 protected val defaultDeserializer: StdDeserializer<TEntity>,
                                                                 protected val idFieldName: String,
                                                                 protected val entityClass: Class<TEntity>)

    : StdDeserializer<TEntity>(entityClass), ResolvableDeserializer, ContextualDeserializer {

    companion object {
        private val log = LoggerFactory.getLogger(PersistedEntityDeserializerBase::class.java)
    }


    // thanks so much to StaxMan (https://stackoverflow.com/a/48415539) for figuring out that we need to implement these
    // two interfaces and pass their call onto defaultDeserializer to get rid of JsonMappingException: No _valueDeserializer assigned

    override fun resolve(ctxt: DeserializationContext?) {
        (defaultDeserializer as? ResolvableDeserializer)?.resolve(ctxt)
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*>? {
        (defaultDeserializer as? ContextualDeserializer)?.createContextual(ctxt, property) // so that BeanDeserializer initializes its property deserializers

        return this
    }


    @Throws(JsonProcessingException::class, IOException::class)
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): TEntity? {
        tryToDeserializePersistedEntity(parser, context)?.let { persistedEntity ->
            return persistedEntity
        }

        try {
            return defaultDeserializer.deserialize(parser, context)
        } catch (e: Exception) {
            log.error("Could not deserialize ${entityClass.simpleName} with default serializer", e)
        }

        return null
    }

    protected open fun tryToDeserializePersistedEntity(parser: JsonParser?, context: DeserializationContext?): TEntity? {
        parser?.let { jsonParser ->
            try {
                val location = jsonParser.currentLocation

                location.sourceRef?.let { sourceRef ->
                    val objectEndIndex = sourceRef.toString().indexOf('}', location.columnNr)

                    if (objectEndIndex > 0) {
                        return tryToDeserializePersistedEntity(jsonParser, location, objectEndIndex)
                    }
                }
            } catch(e: Exception) {
                log.error("Could not deserialize ${entityClass.simpleName}", e)
            }
        }

        return null
    }

    protected open fun tryToDeserializePersistedEntity(jsonParser: JsonParser, location: JsonLocation, objectEndIndex: Int): TEntity? {
        val currentSubTree = location.sourceRef.toString().substring(location.columnNr, objectEndIndex)

        if (currentSubTree.contains(idFieldName)) {
            val node = jsonParser.codec.readTree<ObjectNode>(jsonParser)

            val idNode = node.get(idFieldName)

            if (idNode != null) {
                val entityId = idNode.asText()

                return entityService.retrieve(entityId)
            }
        }

        return null
    }

}