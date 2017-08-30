package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import tornadofx.*


abstract class EntitiesListView: View() {

    abstract fun searchEntities(query: String)

}