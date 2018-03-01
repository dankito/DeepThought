package net.dankito.service.data.messages

import net.dankito.deepthought.model.Item
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class ItemChanged(entity: Item, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Item>(entity, changeType, source)