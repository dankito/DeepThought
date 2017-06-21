package net.dankito.service.data.messages

import net.dankito.deepthought.model.Tag


class TagChanged(entity: Tag, changeType: EntityChangeType): EntityChanged<Tag>(entity, changeType)