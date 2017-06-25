package net.dankito.deepthought.javafx.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javafx.application.Platform
import javafx.css.Styleable
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Window


object FXUtils {

    val HtmlEditorDefaultText = "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>"

    val SizeMaxValue: Double? = java.lang.Double.MAX_VALUE


    fun runOnUiThread(runnable: () -> Unit) {
        if (Platform.isFxApplicationThread()) {
            runnable()
        } else {
            Platform.runLater(runnable)
        }
    }

    fun ensureNodeOnlyUsesSpaceIfVisible(node: Node) {
        node.managedProperty().bind(node.visibleProperty())
    }

    fun setBackgroundToColor(region: Region, color: Color) {
        region.background = Background(BackgroundFill(color, CornerRadii(0.0), Insets(0.0)))
    }


    // thanks for this code to https://stackoverflow.com/questions/12837592/how-to-scroll-to-make-a-node-within-the-content-of-a-scrollpane-visible
    fun scrollToNode(pane: ScrollPane, node: Node) {
        val width = pane.content.boundsInLocal.width
        val height = pane.content.boundsInLocal.height

        val x = node.boundsInParent.maxX
        val y = node.boundsInParent.maxY

        // scrolling values range from 0 to 1
        pane.vvalue = y / height
        pane.hvalue = x / width

        // just for usability
        node.requestFocus()
    }


    fun showSplitPaneDividers(splitPane: SplitPane, show: Boolean) {
        splitPane.lookupAll(".split-pane-divider").stream()
                .forEach { div -> div.isMouseTransparent = !show }
    }

    fun getNodeScreenCoordinates(node: Node): Point2D {
        // thanks for pointing me in the right direction how to calculate a Node's Screen position to: http://blog.crisp.se/2012/08/29/perlundholm/window-scene-and-node-coordinates-in-javafx
        val scene = node.scene
        val window = scene.window

        val windowCoord = Point2D(window.x, window.y)
        val sceneCoord = Point2D(scene.x, scene.y)
        val nodeCoord = node.localToScene(0.0, 0.0)

        return Point2D(Math.round(windowCoord.x + sceneCoord.x + nodeCoord.x).toDouble(),
                Math.round(windowCoord.y + sceneCoord.y + nodeCoord.y).toDouble())
    }

    fun getScreenWindowLeftUpperCornerIsIn(window: Window): Screen? {
        val screens = Screen.getScreensForRectangle(window.x, window.y, 1.0, 1.0)
        if (screens.size > 0)
            return screens[0]

        return null
    }


    private fun isSelectAllEvent(event: KeyEvent): Boolean {
        return event.isShortcutDown && event.code == KeyCode.A
    }

    private fun isPasteEvent(event: KeyEvent): Boolean {
        return event.isShortcutDown && event.code == KeyCode.V
    }

    private fun isCharacterKeyReleased(event: KeyEvent): Boolean {
        // Make custom changes here..
        when (event.code) {
            KeyCode.ALT, KeyCode.COMMAND, KeyCode.CONTROL, KeyCode.SHIFT -> return false
            else -> return true
        }
    }


    fun addStyleToCurrentStyle(node: Node, styleToAdd: String) {
        val style = addStyleToCurrentStyleString(node, styleToAdd)
        node.style = style
    }

    fun addStyleToCurrentStyle(item: MenuItem, styleToAdd: String) {
        val style = addStyleToCurrentStyleString(item, styleToAdd)
        item.style = style
    }

    private fun addStyleToCurrentStyleString(styleable: Styleable, styleToAdd: String): String {
        var style: String? = styleable.style
        style = if (style == null) styleToAdd else style + " " + styleToAdd
        return style
    }

    fun isNoModifierPressed(event: KeyEvent): Boolean {
        return event.isControlDown == false && event.isShiftDown == false && event.isAltDown == false && event.isMetaDown == false && event.isShortcutDown == false
    }

}
