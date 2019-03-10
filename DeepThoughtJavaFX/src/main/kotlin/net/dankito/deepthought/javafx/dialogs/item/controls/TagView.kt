package net.dankito.deepthought.javafx.dialogs.item.controls

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import net.dankito.utils.javafx.util.FXUtils
import tornadofx.*


class TagView(itemText: String, showRemoveButton: Boolean = false, private var itemRemovedListener: (() -> Unit)? = null) : View() {

    private val itemTextProperty = SimpleStringProperty(itemText)


    override val root = hbox {
        usePrefSize = true
        prefWidth = Region.USE_COMPUTED_SIZE
        prefHeight = Region.USE_COMPUTED_SIZE
        useMaxSize = true

        background = Background(BackgroundFill(Color.valueOf("#f2f2f2"), CornerRadii(12.0), Insets.EMPTY))

        paddingLeft = 8.0
        paddingRight = 8.0

        if(showRemoveButton) {
            button("x") {
                textAlignment = TextAlignment.CENTER
                font = Font.font(13.0)
                textFill = Color.valueOf("#9a9a9a")
                FXUtils.setBackgroundToColor(this, Color.TRANSPARENT)

                action { itemRemovedListener?.invoke() }

                hboxConstraints {
                    marginRight = 1.0
                }
            }
        }

        label(itemTextProperty) {
            usePrefSize = true
            prefWidth = Region.USE_COMPUTED_SIZE
            useMaxSize = true

            textAlignment = TextAlignment.CENTER
            font = Font.font(13.0)
            textFill = Color.valueOf("#9a9a9a")
            FXUtils.setBackgroundToColor(this, Color.TRANSPARENT)
        }

        tag = this@TagView // so that we can get TagView instance when iterating over collectionPreviewPane's children (= this.root)

    }


    fun update(itemText: String, itemRemovedListener: (() -> Unit)? = null) {
        itemTextProperty.value = itemText

        this.itemRemovedListener = itemRemovedListener
    }

}