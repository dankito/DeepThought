package net.dankito.service.data.event

import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.synchronization.model.BaseEntity


interface IEntityChangedNotifier {

    fun notifyListenersOfEntityChangeAsync(entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource, didChangesAffectingDependentEntities: Boolean = false)

    fun notifyListenersOfEntityChange(entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource, didChangesAffectingDependentEntities: Boolean = false, isDependentChange: Boolean = false)

}