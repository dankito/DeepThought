package net.dankito.service.data.messages

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig


class ArticleSummaryExtractorConfigChanged(entity: ArticleSummaryExtractorConfig, changeType: EntityChangeType, source: EntityChangeSource)
    : EntityChanged<ArticleSummaryExtractorConfig>(entity, changeType, source)