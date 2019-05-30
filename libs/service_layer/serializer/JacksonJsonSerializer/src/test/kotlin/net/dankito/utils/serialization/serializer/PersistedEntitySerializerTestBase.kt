package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.deepthought.model.BaseEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test


abstract class PersistedEntitySerializerTestBase<T : BaseEntity>(protected val entityClass: Class<T>) {

    companion object {
        protected const val ID = "entity-id"
    }


    protected abstract fun createSerializer(): PersistedEntitySerializerBase<T>

    protected abstract fun createEntity(): T

    protected abstract fun getIdFieldName(): String


    protected val underTest: PersistedEntitySerializerBase<T> = createSerializer()

    protected val mapper = ObjectMapper()


    @Before
    fun setUp() {
        val module = SimpleModule()

        module.addSerializer(entityClass, underTest)

        mapper.registerModule(module)
    }


    @Test
    fun serializePersistedEntity() {

        // given
        val entity = createEntity()
        entity.id = ID

        // when
        val result = mapper.writeValueAsString(entity)

        // then

        assertThat(result).isEqualTo("{\"${getIdFieldName()}\":\"$ID\"}")
    }

    @Test
    fun serializeUnpersistedEntity() {

        // given
        val entity = createEntity()

        // when
        val result = mapper.writeValueAsString(entity)

        // then

        assertThat(result).doesNotContain(getIdFieldName())
    }

}