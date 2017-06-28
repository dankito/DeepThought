package net.dankito.service.data.messages

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig


class ArticleSummaryExtractorConfigChanged(entity: ArticleSummaryExtractorConfig, changeType: EntityChangeType): EntityChanged<ArticleSummaryExtractorConfig>(entity, changeType)