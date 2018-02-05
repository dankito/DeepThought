package net.dankito.service.data.messages

import net.dankito.deepthought.model.Item


class ItemChanged(entity: Item, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Item>(entity, changeType, source)