package net.dankito.service.data

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class SeriesService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<Series>(Series::class.java, dataManager, entityChangedNotifier)