package net.dankito.service.data.messages

import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.eventbus.messages.IEventBusMessage


abstract class EntityChanged<TEntity: BaseEntity>(val entity: TEntity, val changeType: EntityChangeType) : IEventBusMessage