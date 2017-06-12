package net.dankito.service.data.messages

import net.dankito.deepthought.model.BaseEntity
import net.dankito.service.eventbus.messages.IEventBusMessage


class EntitiesOfTypeChanged(val entityType: Class<out BaseEntity>) : IEventBusMessage