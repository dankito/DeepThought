package net.dankito.deepthought.javafx.res

import javafx.scene.paint.Color


class Colors {

    companion object {
        // Oliver's green based design
        val Primary = Color.valueOf("#359e7d")
        val PrimaryDark = Color.valueOf("#22685d")
        val Accent = Color.valueOf("#078176")

        // argb = (0.949999988079071, 0.529411792755127, 0.8078431487083435, 0.9803921580314636)
        val ClipboardContentPopupBackgroundColor = Color.LIGHTSKYBLUE.deriveColor(0.0, 1.0, 1.0, 0.95)

        // argb = (1.0, 0.3921568691730499, 0.5843137502670288, 0.929411768913269)
        val ClipboardContentPopupOptionMouseOverColor = Color.CORNFLOWERBLUE
    }

}