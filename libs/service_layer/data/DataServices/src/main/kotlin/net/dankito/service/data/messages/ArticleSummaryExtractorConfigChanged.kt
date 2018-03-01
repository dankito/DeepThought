package net.dankito.service.data.messages

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType


class ArticleSummaryExtractorConfigChanged(entity: ArticleSummaryExtractorConfig, changeType: EntityChangeType, source: EntityChangeSource)
    : EntityChanged<ArticleSummaryExtractorConfig>(entity, changeType, source)