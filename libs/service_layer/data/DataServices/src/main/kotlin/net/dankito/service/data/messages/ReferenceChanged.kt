package net.dankito.service.data.messages

import net.dankito.deepthought.model.Reference


class ReferenceChanged(entity: Reference, changeType: EntityChangeType): EntityChanged<Reference>(entity, changeType)