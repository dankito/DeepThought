package net.dankito.deepthought.model.enums


class ApplicationLanguage : ExtensibleEnumeration {

    companion object {
        private const val serialVersionUID = -446610923063763955L
    }


    var languageKey: String = ""
        private set


    private constructor() : this("")

    constructor(name: String) : super(name) {}

    constructor(nameResourceKey: String, languageKey: String, isSystemValue: Boolean, sortOrder: Int) : super(nameResourceKey, isSystemValue, sortOrder) {
        this.languageKey = languageKey
    }

}
