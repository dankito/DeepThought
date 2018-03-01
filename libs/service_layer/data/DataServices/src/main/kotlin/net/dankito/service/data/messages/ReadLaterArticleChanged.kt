package net.dankito.service.data.messages

import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class ReadLaterArticleChanged(entity: ReadLaterArticle, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<ReadLaterArticle>(entity, changeType, source)