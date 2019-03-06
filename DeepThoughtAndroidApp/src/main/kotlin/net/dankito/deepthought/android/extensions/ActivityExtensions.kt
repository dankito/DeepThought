package net.dankito.deepthought.android.extensions

import android.app.Activity
import net.dankito.deepthought.android.ui.theme.AppThemes
import net.dankito.deepthought.android.ui.theme.ThemeConstants
import net.dankito.utils.android.extensions.themeName
import net.dankito.utils.android.ui.theme.Theme


val Activity.getAppTheme: Theme
    get() {
        return if (hasDarkTheme) AppThemes.Dark else AppThemes.Light
    }

/**
 * Returns true if attribute [net.dankito.utils.android.R.attr.themeName] is set on theme / style resource and
 * its value equals [net.dankito.deepthought.android.ui.theme.ThemeConstants.LightThemeName].
 */
val Activity.hasLightTheme: Boolean
    get() {
        return ThemeConstants.LightThemeName == themeName
    }

/**
 * Returns true if attribute [net.dankito.utils.android.R.attr.themeName] is set on theme / style resource and
 * its value equals [net.dankito.deepthought.android.ui.theme.ThemeConstants.DarkThemeName].
 */
val Activity.hasDarkTheme: Boolean
    get() {
        return ThemeConstants.DarkThemeName == themeName
    }