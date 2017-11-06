package net.dankito.service.data

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeType


class SeriesService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<Series>(Series::class.java, dataManager, entityChangedNotifier) {

    // as other components may depend on that a newly created Series, in this special case wait till Series is index and that all
    override fun callEntitiesUpdatedListenersForCreatedEntity(entity: Series) {
        callEntitiesUpdatedListenersSynchronously(entity, EntityChangeType.Created)
    }

}