package net.dankito.faviconextractor


data class Size(val width: Int, val height: Int) : Comparable<Size> {

    fun isSquare(): Boolean {
        return width == height
    }


    override fun compareTo(other: Size): Int {
        if(width == other.width) {
            return height.compareTo(other.height)
        }

        return width.compareTo(other.width)
    }


    override fun toString(): String {
        return "$width x $height"
    }

}