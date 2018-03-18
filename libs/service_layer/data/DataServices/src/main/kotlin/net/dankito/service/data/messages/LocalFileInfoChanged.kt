package net.dankito.service.data.messages

import net.dankito.deepthought.model.LocalFileInfo


class LocalFileInfoChanged(entity: LocalFileInfo, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<LocalFileInfo>(entity, changeType, source)