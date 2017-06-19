package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.model.Entry
import tornadofx.*


class EntryView : View() {

    val controller: MainWindowController by inject()


    override val root = tableview<Entry> {
        column("index", Entry::entryIndex).weigthedWidth(1.0)
        column("reference", Entry::reference).weigthedWidth(4.0)
        column("content", Entry::content).weigthedWidth(4.0)

        columnResizePolicy = SmartResize.POLICY

        bindSelected(controller.entryModel)

        items = controller.entries
    }

}