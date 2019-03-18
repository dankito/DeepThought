package net.dankito.deepthought.javafx.dialogs

import javafx.scene.image.Image
import javafx.stage.Stage
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.utils.image.ImageCache
import net.dankito.utils.windowregistry.window.javafx.ui.JavaFXWindow
import tornadofx.*
import javax.inject.Inject


abstract class DialogFragment : JavaFXWindow() {

    @Inject
    protected lateinit var imageCache: ImageCache


    init {
        AppComponent.component.inject(this)
    }


    override fun setIcon(dialogStage: Stage, iconUrl: String) {
        if (iconUrl.startsWith("http", true) == false) { // one of our application icons
            dialogStage.icons.add(Image(iconUrl))
        }
        else { // cache icon so that it only gets retrieved once
            imageCache.getCachedForRetrieveIconForUrlAsync(iconUrl) { result ->
                result.result?.let { iconPath ->
                    runLater {
                        dialogStage.icons.add(Image(iconPath.toURI().toString()))
                    }
                }
            }
        }
    }

}