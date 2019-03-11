package net.dankito.deepthought.javafx.dialogs

import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.utils.image.ImageCache
import tornadofx.*
import javax.inject.Inject


abstract class DialogFragment : Fragment() {

    @Inject
    protected lateinit var imageCache: ImageCache


    init {
        AppComponent.component.inject(this)
    }


    fun show(title: String? = null, iconUrl: String? = null, stageStyle: StageStyle = StageStyle.DECORATED, modality: Modality = Modality.NONE, owner: Window? = null) : Stage {
        val dialogStage = Stage()

        dialogStage.title = title
        iconUrl?.let { setIcon(dialogStage, it) }
        owner?.let { dialogStage.initOwner(it) }

        dialogStage.initModality(modality)
        dialogStage.initStyle(stageStyle)

        val scene = Scene(this.root)

        dialogStage.scene = scene

        dialogStage.show()
        dialogStage.requestFocus()

        this.modalStage = dialogStage

        return dialogStage
    }

    private fun setIcon(dialogStage: Stage, iconUrl: String) {
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