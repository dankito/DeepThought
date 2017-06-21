package net.dankito.deepthought.ui.presenter


interface IMainViewSectionPresenter {

    fun getAndShowAllEntities()

    fun getLastSearchTerm(): String

    fun cleanUp()

}