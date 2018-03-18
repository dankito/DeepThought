package net.dankito.service.data.messages

import net.dankito.deepthought.model.Source


class SourceChanged(entity: Source, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Source>(entity, changeType, source)