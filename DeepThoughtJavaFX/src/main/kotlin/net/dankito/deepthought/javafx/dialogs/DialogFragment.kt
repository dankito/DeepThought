package net.dankito.deepthought.javafx.dialogs

import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import tornadofx.*




abstract class DialogFragment : Fragment() {

    fun show(stageStyle: StageStyle = StageStyle.DECORATED, modality: Modality = Modality.NONE, owner: Window? = null) : Stage {
        val dialogStage = Stage()

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
}