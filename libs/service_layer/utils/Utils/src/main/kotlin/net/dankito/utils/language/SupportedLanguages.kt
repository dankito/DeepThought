package net.dankito.utils.language


class SupportedLanguages {

    val languagesMap = HashMap<String, Language>()


    init {
        createSupportedLanguages().forEach {
            languagesMap.put(it.languageKey, it)
        }
    }


    fun getLanguageForKey(key: String): Language? {
        return languagesMap[key]
    }

    fun getLanguages(): List<Language> {
        return languagesMap.values.toList()
    }


    private fun createSupportedLanguages(): List<Language> {
        val languages = ArrayList<Language>()

        languages.add(Language("en", "English", "English", 1))
        languages.add(Language("de", "Deutsch", "German", 2))
        languages.add(Language("es", "Español", "Spanish", 3))
        languages.add(Language("fr", "Français", "French", 4))
        languages.add(Language("it", "Italiano", "Italian", 5))
        languages.add(Language("ar", "العربية", "Arabic", 6))
        languages.add(Language("bg", "Български", "Bulgarian", 7))
        languages.add(Language("cs", "Čeština", "Czech", 8))
        languages.add(Language("da", "Dansk", "Danish", 9))
        languages.add(Language("el", "Ελληνικά", "Greek", 10))
        languages.add(Language("fa", "فارسی", "Persian", 11))
        languages.add(Language("fi", "Suomi", "Finnish", 12))
        languages.add(Language("hi", "हिन्दी", "Hindi", 13))
        languages.add(Language("hu", "Magyar", "Hungarian", 14))
        languages.add(Language("id", "Bahasa Indonesia", "Indonesian", 15))
        languages.add(Language("ja", "日本語", "Japanese", 16))
        languages.add(Language("ko", "한국어", "Korean", 17))
        languages.add(Language("nl", "Nederlands", "Dutch", 18))
        languages.add(Language("no", "Norsk bokmål", "Norwegian", 19))
        languages.add(Language("pt", "Português", "Portuguese", 20))
        languages.add(Language("ro", "Română", "Romanian", 21))
        languages.add(Language("ru", "Русский", "Russian", 22))
        languages.add(Language("sv", "Svenska", "Swedish", 23))
        languages.add(Language("th", "ไทย", "Thai", 24))
        languages.add(Language("tr", "Türkçe", "Turkish", 25))
        languages.add(Language("zh-cn", "中文", "Simplified Chinese", 26))
        languages.add(Language("zh-tw", "文言", "Traditional Chinese", 27))

        languages.add(Language("ar", "Afrikaans", "Afrikaans", 28))
        languages.add(Language("bn", "বাংলা", "Bengali", 29))
        languages.add(Language("gu", "ગુજરાતી", "Gujarati", 30))
        languages.add(Language("he", "עברית", "Hebrew", 31))
        languages.add(Language("hr", "Hrvatski", "Croatian", 32))
        languages.add(Language("kn", "ಕನ್ನಡ", "Kannada", 33))
        languages.add(Language("mk", "Македонски", "Macedonian", 34))
        languages.add(Language("ml", "മലയാളം", "Malayalam", 35))
        languages.add(Language("mr", "मराठी", "Marathi", 36))
        languages.add(Language("ne", "नेपाली", "Nepali", 37))
        languages.add(Language("pa", "ਪੰਜਾਬੀ", "Punjabi", 38))
        languages.add(Language("pl", "Polski", "Polish", 39))
        languages.add(Language("sk", "Slovenčina", "Slovak", 40))
        languages.add(Language("so", "Soomaaliga", "Somali", 41))
        languages.add(Language("sq", "Shqip", "Albanian", 42))
        languages.add(Language("sw", "Kiswahili", "Swahili", 43))
        languages.add(Language("ta", "தமிழ்", "Tamil", 44))
        languages.add(Language("te", "తెలుగు", "Telugu", 45))
        languages.add(Language("tl", "Tagalog", "Tagalog", 46))
        languages.add(Language("uk", "Українська", "Ukrainian", 47))
        languages.add(Language("ur", "اردو", "Urdu", 48))
        languages.add(Language("vi", "Tiếng Việt", "Vietnamese", 49))

        return languages
    }
}