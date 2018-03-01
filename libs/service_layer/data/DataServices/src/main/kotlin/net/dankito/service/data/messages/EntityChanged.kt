package net.dankito.service.data.messages

import net.dankito.service.eventbus.messages.IEventBusMessage
import net.dankito.synchronization.model.BaseEntity
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


abstract class EntityChanged<TEntity: BaseEntity>(val entity: TEntity, val changeType: EntityChangeType, val source: EntityChangeSource, var isDependentChange: Boolean = false)
    : IEventBusMessage