package net.dankito.deepthought.javafx.res

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight


class Fonts {

    companion object {
        // copied from Android/res/styles.xml
        val Header1Font: Font = Font.font("serif", FontWeight.BOLD, 14.0)
        val Header1FontNonBold: Font = Font.font(Header1Font.family, FontWeight.NORMAL, Header1Font.size)
        val Header1TextColor: Paint = Color.valueOf("#212121")

        val Header2Font: Font = Font.font("Georgia", FontPosture.ITALIC, 13.0)
        val Header2TextColor: Paint = Color.valueOf("#999999")

        val TextBodyFont: Font = Font.font(12.0)
        val TextBodyTextColor: Paint = Color.valueOf("#7f7f7f")
    }
}