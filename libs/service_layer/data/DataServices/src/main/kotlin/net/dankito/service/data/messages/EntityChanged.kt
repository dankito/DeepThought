package net.dankito.service.data.messages

import net.dankito.synchronization.model.BaseEntity
import net.dankito.service.eventbus.messages.IEventBusMessage


abstract class EntityChanged<TEntity: BaseEntity>(val entity: TEntity, val changeType: EntityChangeType, val source: EntityChangeSource, var isDependentChange: Boolean = false)
    : IEventBusMessage