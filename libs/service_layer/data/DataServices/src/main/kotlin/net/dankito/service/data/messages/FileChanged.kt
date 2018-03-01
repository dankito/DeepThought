package net.dankito.service.data.messages

import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class FileChanged(entity: DeepThoughtFileLink, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<DeepThoughtFileLink>(entity, changeType, source)