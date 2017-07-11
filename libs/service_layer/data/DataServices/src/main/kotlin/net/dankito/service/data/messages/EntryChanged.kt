package net.dankito.service.data.messages

import net.dankito.deepthought.model.Entry


class EntryChanged(entity: Entry, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Entry>(entity, changeType, source)