package net.dankito.service.data.messages

import net.dankito.deepthought.model.DeepThoughtFileLink


class FileChanged(entity: DeepThoughtFileLink, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<DeepThoughtFileLink>(entity, changeType, source)