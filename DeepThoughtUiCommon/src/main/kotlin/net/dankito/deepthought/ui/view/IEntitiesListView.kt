package net.dankito.deepthought.ui.view

import net.dankito.synchronization.model.BaseEntity


interface IEntitiesListView<in T: BaseEntity> {

    fun showEntities(entities: List<T>)

}