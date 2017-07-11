package net.dankito.service.data.messages

import net.dankito.deepthought.model.Reference


class ReferenceChanged(entity: Reference, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<Reference>(entity, changeType, source)