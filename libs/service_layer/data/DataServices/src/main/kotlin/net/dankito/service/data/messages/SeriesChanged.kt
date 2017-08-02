package net.dankito.service.data.messages

import net.dankito.deepthought.model.Series


class SeriesChanged(entity: Series, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Series>(entity, changeType, source)