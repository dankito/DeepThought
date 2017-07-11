package net.dankito.service.data.messages

import net.dankito.deepthought.model.ReadLaterArticle


class ReadLaterArticleChanged(entity: ReadLaterArticle, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<ReadLaterArticle>(entity, changeType, source)