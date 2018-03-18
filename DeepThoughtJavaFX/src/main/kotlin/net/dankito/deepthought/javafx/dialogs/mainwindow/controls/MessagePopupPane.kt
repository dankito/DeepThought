package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.geometry.Insets
import javafx.scene.layout.VBox
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContent
import net.dankito.deepthought.service.data.DataManager
import tornadofx.*
import javax.inject.Inject


class MessagePopupPane(dataManager: DataManager) : View() {


    @Inject
    protected lateinit var clipboardWatcher: JavaFXClipboardWatcher



    init {
        dataManager.addInitializationListener {
            AppComponent.component.inject(this)

            clipboardWatcher.addClipboardOptionsChangedListener { addClipboardContentPopup(it) }
        }
    }


    override val root = vbox {

    }


    private fun addClipboardContentPopup(options: OptionsForClipboardContent) {
        val clipboardContentPopup = ClipboardContentPopup(clipboardWatcher)
        addPopup(clipboardContentPopup)

        clipboardContentPopup.setClipboardContentOptions(options) // add ClipboardContentPopup to root first so that its scene is set and KeyCombinations can take effect
    }

    private fun addPopup(popup: View) {
        root.add(popup)

        VBox.setMargin(popup.root, Insets(6.0, 0.0, 0.0, 0.0)) // so that there's a space to next popup

        popup.root.visibleProperty().addListener { _, _, newValue ->
            if(newValue == false) { // Popup has been hidden
                root.children.remove(popup.root)
            }
        }
    }

}