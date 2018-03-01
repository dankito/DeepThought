package net.dankito.service.data.messages

import net.dankito.deepthought.model.Tag
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class TagChanged(entity: Tag, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Tag>(entity, changeType, source)