package net.dankito.service.data.messages

import net.dankito.deepthought.model.Source


class ReferenceChanged(entity: Source, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Source>(entity, changeType, source)