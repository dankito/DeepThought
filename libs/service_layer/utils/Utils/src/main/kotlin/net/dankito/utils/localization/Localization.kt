package net.dankito.utils.localization

import org.slf4j.LoggerFactory
import java.util.*


class Localization {

    companion object {
        private const val MessagesResourceBundleName = "Messages"


        private val log = LoggerFactory.getLogger(Localization::class.java)
    }


    var languageLocale: Locale = Locale.getDefault()
        set(value) {
            field = value
            Locale.setDefault(field)

            tryToLoadMessagesResourceBundle(field)
        }

    var messagesResourceBundle: ResourceBundle
        private set


    init {
        this.messagesResourceBundle = createEmptyResourceBundle()

        tryToLoadMessagesResourceBundle(languageLocale)
    }


    fun getLocalizedString(resourceKey: String): String {
        try {
            messagesResourceBundle?.let { messagesResourceBundle ->
                return messagesResourceBundle.getString(resourceKey)
            }
        } catch (e: Exception) {
            log.error("Could not get Resource for key {} from String Resource Bundle {}", resourceKey, MessagesResourceBundleName)
        }

        return resourceKey
    }

    fun getLocalizedString(resourceKey: String, vararg formatArguments: Any): String {
        return String.format(getLocalizedString(resourceKey), *formatArguments)
    }


    private fun tryToLoadMessagesResourceBundle(languageLocale: Locale) {
        try {
            messagesResourceBundle = ResourceBundle.getBundle(MessagesResourceBundleName, languageLocale, UTF8ResourceBundleControl())
        } catch (e: Exception) {
            log.error("Could not load $MessagesResourceBundleName. No Strings will now be translated, only their resource keys will be displayed.", e)
        }
    }

    private fun createEmptyResourceBundle(): ResourceBundle {
        return object : ResourceBundle() {

            private val emptyEnumeration = Collections.enumeration(emptyList<String>())

            override fun getKeys(): Enumeration<String> {
                return emptyEnumeration
            }

            override fun handleGetObject(key: String?): Any? {
                return null
            }

        }
    }

}
