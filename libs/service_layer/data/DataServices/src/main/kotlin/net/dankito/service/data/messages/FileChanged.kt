package net.dankito.service.data.messages

import net.dankito.deepthought.model.FileLink


class FileChanged(entity: FileLink, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<FileLink>(entity, changeType, source)