package net.dankito.utils.version


class Version(val major: Int, val minor: Int, val patch: Int = 0, val build: String? = null) {

    private constructor() : this(0, 0, 0) // for Jackson
}