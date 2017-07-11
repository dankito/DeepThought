package net.dankito.service.data.messages

import net.dankito.deepthought.model.Tag


class TagChanged(entity: Tag, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Tag>(entity, changeType, source)