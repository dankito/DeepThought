package net.dankito.deepthought.ui.presenter


interface IMainViewSectionPresenter {

    fun getLastSearchTerm(): String


    fun viewBecomesVisible()

    fun viewGetsHidden()

}