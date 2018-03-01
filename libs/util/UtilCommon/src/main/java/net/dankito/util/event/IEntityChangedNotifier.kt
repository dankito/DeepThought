package net.dankito.util.event


interface IEntityChangedNotifier<T> {

    fun notifyListenersOfEntityChangeAsync(entity: T, changeType: EntityChangeType, source: EntityChangeSource, didChangesAffectingDependentEntities: Boolean = false)

    fun notifyListenersOfEntityChange(entity: T, changeType: EntityChangeType, source: EntityChangeSource, didChangesAffectingDependentEntities: Boolean = false, isDependentChange: Boolean = false)

}