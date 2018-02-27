package net.dankito.service.data.messages

import net.dankito.synchronization.model.BaseEntity
import net.dankito.service.eventbus.messages.IEventBusMessage


class EntitiesOfTypeChanged(val entityType: Class<out BaseEntity>, val changeType: EntityChangeType, val source: EntityChangeSource, val isDependentChange: Boolean = false)
    : IEventBusMessage