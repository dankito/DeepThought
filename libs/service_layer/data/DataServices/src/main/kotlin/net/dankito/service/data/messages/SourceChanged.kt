package net.dankito.service.data.messages

import net.dankito.deepthought.model.Source
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class SourceChanged(entity: Source, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Source>(entity, changeType, source)