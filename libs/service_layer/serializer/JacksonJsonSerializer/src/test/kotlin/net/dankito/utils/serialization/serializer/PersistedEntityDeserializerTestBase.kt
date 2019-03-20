package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.nhaarman.mockito_kotlin.doReturn
import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.data.EntityServiceBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock


abstract class PersistedEntityDeserializerTestBase<T : BaseEntity>(protected val entityClass: Class<T>) {

    companion object {
        protected const val ID = "entity-id"
    }


    protected abstract fun createEntityServiceMock(): EntityServiceBase<T>

    protected abstract fun createDeserializer(entityService: EntityServiceBase<T>): PersistedEntityDeserializerBase<T>

    protected abstract fun getIdFieldName(): String


    protected val mapper = ObjectMapper()

    protected val entityMock: T = mock(entityClass)

    protected val entityServiceMock: EntityServiceBase<T> = createEntityServiceMock()

    protected val underTest: PersistedEntityDeserializerBase<T> = createDeserializer(entityServiceMock)


    @Before
    fun setUp() {
        val module = SimpleModule()

        module.addDeserializer(entityClass, underTest)

        mapper.registerModule(module)

        doReturn(ID).`when`(entityMock).id

        doReturn(entityMock).`when`(entityServiceMock).retrieve(ID)
    }


    @Test
    fun deserialize() {

        // given
        val serializedEntity = "{\"${getIdFieldName()}\":\"$ID\"}"

        // when
        val result = mapper.readValue(serializedEntity, entityClass)

        // then
        assertThat(result).isEqualTo(entityMock)
        assertThat(result.id).isEqualTo(ID)
    }

}