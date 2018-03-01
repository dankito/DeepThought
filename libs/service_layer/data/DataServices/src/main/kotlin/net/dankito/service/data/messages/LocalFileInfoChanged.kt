package net.dankito.service.data.messages

import net.dankito.synchronization.model.LocalFileInfo
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class LocalFileInfoChanged(entity: LocalFileInfo, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<LocalFileInfo>(entity, changeType, source)