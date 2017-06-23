package net.dankito.service.data.messages

import net.dankito.deepthought.model.ReadLaterArticle


class ReadLaterArticleChanged(entity: ReadLaterArticle, changeType: EntityChangeType): EntityChanged<ReadLaterArticle>(entity, changeType)