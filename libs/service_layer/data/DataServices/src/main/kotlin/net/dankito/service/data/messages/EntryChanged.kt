package net.dankito.service.data.messages

import net.dankito.deepthought.model.Entry


class EntryChanged(entity: Entry, changeType: EntityChangeType): EntityChanged<Entry>(entity, changeType)