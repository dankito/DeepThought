package net.dankito.service.data.messages

import net.dankito.deepthought.model.Series
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class SeriesChanged(entity: Series, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Series>(entity, changeType, source)