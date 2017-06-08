package net.dankito.faviconextractor

import net.dankito.data_access.network.webclient.OkHttpWebClient
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class FaviconComparatorTest {

    private val underTest = FaviconComparator(OkHttpWebClient())


    @Test
    fun getBestIconForWikipedia() {
        val bestIcon = Favicon("https://www.wikipedia.org/static/apple-touch/wikipedia.png", FaviconType.AppleTouch)
        val favicons = listOf<Favicon>(Favicon("https://www.wikipedia.org/static/favicon/wikipedia.ico", FaviconType.ShortcutIcon), bestIcon)

        val result = underTest.getBestIcon(favicons)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForGuardian() {
        val bestIcon = Favicon("https://assets.guim.co.uk/images/2170b16eb045a34f8c79761b203627b4/fallback-logo.png", FaviconType.OpenGraphImage)
        val favicons = listOf<Favicon>(Favicon("https://assets.guim.co.uk/images/favicons/451963ac2e23633472bf48e2856d3f04/152x152.png", FaviconType.AppleTouch, Size(152, 152)),
                Favicon("https://assets.guim.co.uk/images/favicons/1a3f98d8491f8cfdc224089b785da86b/144x144.png", FaviconType.AppleTouch, Size(144, 144)),
                Favicon("https://assets.guim.co.uk/images/favicons/cf23080600002e50f5869c72f5a904bd/120x120.png", FaviconType.AppleTouch, Size(120, 120)),
                Favicon("https://assets.guim.co.uk/images/favicons/f438f6041a4c1d0289e6debd112880c2/114x114.png", FaviconType.AppleTouch, Size(114, 114)),
                Favicon("https://assets.guim.co.uk/images/favicons/b5050517955e7cf1e493ccc53e64ca05/72x72.png", FaviconType.AppleTouch, Size(72, 72)),
                Favicon("https://assets.guim.co.uk/images/favicons/4fd650035a2cebafea4e210990874c64/57x57.png", FaviconType.AppleTouchPrecomposed),
                Favicon("https://assets.guim.co.uk/images/favicons/79d7ab5a729562cebca9c6a13c324f0e/32x32.ico", FaviconType.ShortcutIcon, type = "image/png"),
                Favicon("https://assets.guim.co.uk/images/favicons/f06f6996e193d1ddcd614ea852322d25/windows_tile_144_b.png", FaviconType.MsTileImage),
                bestIcon)

        val result = underTest.getBestIcon(favicons)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForGuardianIncludingRssIcon() {
        val bestIcon = Favicon("https://assets.guim.co.uk/images/2170b16eb045a34f8c79761b203627b4/fallback-logo.png", FaviconType.OpenGraphImage)
        val favicons = listOf<Favicon>(Favicon("https://assets.guim.co.uk/images/guardian-logo-rss.c45beb1bafa34b347ac333af2e6fe23f.png", FaviconType.Icon, Size(250, 40)),
                Favicon("https://assets.guim.co.uk/images/favicons/451963ac2e23633472bf48e2856d3f04/152x152.png", FaviconType.AppleTouch, Size(152, 152)),
                Favicon("https://assets.guim.co.uk/images/favicons/1a3f98d8491f8cfdc224089b785da86b/144x144.png", FaviconType.AppleTouch, Size(144, 144)),
                Favicon("https://assets.guim.co.uk/images/favicons/cf23080600002e50f5869c72f5a904bd/120x120.png", FaviconType.AppleTouch, Size(120, 120)),
                Favicon("https://assets.guim.co.uk/images/favicons/f438f6041a4c1d0289e6debd112880c2/114x114.png", FaviconType.AppleTouch, Size(114, 114)),
                Favicon("https://assets.guim.co.uk/images/favicons/b5050517955e7cf1e493ccc53e64ca05/72x72.png", FaviconType.AppleTouch, Size(72, 72)),
                Favicon("https://assets.guim.co.uk/images/favicons/4fd650035a2cebafea4e210990874c64/57x57.png", FaviconType.AppleTouchPrecomposed),
                Favicon("https://assets.guim.co.uk/images/favicons/79d7ab5a729562cebca9c6a13c324f0e/32x32.ico", FaviconType.ShortcutIcon, type = "image/png"),
                Favicon("https://assets.guim.co.uk/images/favicons/f06f6996e193d1ddcd614ea852322d25/windows_tile_144_b.png", FaviconType.MsTileImage),
                bestIcon)

        val result = underTest.getBestIcon(favicons, returnSquarishOneIfPossible = false)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestSquareIconForGuardianIncludingRssIcon() {
        val bestIcon = Favicon("https://assets.guim.co.uk/images/favicons/451963ac2e23633472bf48e2856d3f04/152x152.png", FaviconType.AppleTouch, Size(152, 152))
        val favicons = listOf<Favicon>(Favicon("https://assets.guim.co.uk/images/guardian-logo-rss.c45beb1bafa34b347ac333af2e6fe23f.png", FaviconType.Icon, Size(250, 40)),
                Favicon("https://assets.guim.co.uk/images/favicons/1a3f98d8491f8cfdc224089b785da86b/144x144.png", FaviconType.AppleTouch, Size(144, 144)),
                Favicon("https://assets.guim.co.uk/images/favicons/cf23080600002e50f5869c72f5a904bd/120x120.png", FaviconType.AppleTouch, Size(120, 120)),
                Favicon("https://assets.guim.co.uk/images/favicons/f438f6041a4c1d0289e6debd112880c2/114x114.png", FaviconType.AppleTouch, Size(114, 114)),
                Favicon("https://assets.guim.co.uk/images/favicons/b5050517955e7cf1e493ccc53e64ca05/72x72.png", FaviconType.AppleTouch, Size(72, 72)),
                Favicon("https://assets.guim.co.uk/images/favicons/4fd650035a2cebafea4e210990874c64/57x57.png", FaviconType.AppleTouchPrecomposed),
                Favicon("https://assets.guim.co.uk/images/favicons/79d7ab5a729562cebca9c6a13c324f0e/32x32.ico", FaviconType.ShortcutIcon, type = "image/png"),
                Favicon("https://assets.guim.co.uk/images/favicons/f06f6996e193d1ddcd614ea852322d25/windows_tile_144_b.png", FaviconType.MsTileImage),
                Favicon("https://assets.guim.co.uk/images/2170b16eb045a34f8c79761b203627b4/fallback-logo.png", FaviconType.OpenGraphImage),
                bestIcon)

        val result = underTest.getBestIcon(favicons, returnSquarishOneIfPossible = true)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForNewYorkTimes() {
        val bestIcon = Favicon("https://static01.nyt.com/images/icons/t_logo_291_black.png", FaviconType.OpenGraphImage)
        val favicons = listOf<Favicon>(Favicon("https://static01.nyt.com/favicon.ico", FaviconType.ShortcutIcon),
                                        Favicon("https://static01.nyt.com/images/icons/ios-ipad-144x144.png", FaviconType.AppleTouchPrecomposed, Size(144, 144)),
                                        Favicon("https://static01.nyt.com/images/icons/ios-iphone-114x144.png", FaviconType.AppleTouchPrecomposed, Size(114, 114)),
                                        Favicon("https://static01.nyt.com/images/icons/ios-default-homescreen-57x57.png", FaviconType.AppleTouchPrecomposed),
                                        bestIcon)

        val result = underTest.getBestIcon(favicons)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconWithMaxSize152ForNewYorkTimes() {
        val bestIcon = Favicon("https://static01.nyt.com/images/icons/ios-ipad-144x144.png", FaviconType.AppleTouchPrecomposed, Size(144, 144))
        val favicons = listOf<Favicon>(Favicon("https://static01.nyt.com/favicon.ico", FaviconType.ShortcutIcon),
                Favicon("https://static01.nyt.com/images/icons/t_logo_291_black.png", FaviconType.OpenGraphImage),
                Favicon("https://static01.nyt.com/images/icons/ios-iphone-114x144.png", FaviconType.AppleTouchPrecomposed, Size(114, 114)),
                Favicon("https://static01.nyt.com/images/icons/ios-default-homescreen-57x57.png", FaviconType.AppleTouchPrecomposed),
                bestIcon)

        val result = underTest.getBestIcon(favicons, minSize = 32, maxSize = 152)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconWithMaxSize112ForNewYorkTimes() {
        val bestIcon = Favicon("https://static01.nyt.com/images/icons/ios-default-homescreen-57x57.png", FaviconType.AppleTouchPrecomposed)
        val favicons = listOf<Favicon>(Favicon("https://static01.nyt.com/favicon.ico", FaviconType.ShortcutIcon),
                Favicon("https://static01.nyt.com/images/icons/t_logo_291_black.png", FaviconType.OpenGraphImage),
                Favicon("https://static01.nyt.com/images/icons/ios-ipad-144x144.png", FaviconType.AppleTouchPrecomposed, Size(144, 144)),
                Favicon("https://static01.nyt.com/images/icons/ios-iphone-114x144.png", FaviconType.AppleTouchPrecomposed, Size(114, 114)),
                bestIcon)

        val result = underTest.getBestIcon(favicons, minSize = 32, maxSize = 112)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForDieZeit() {
        val bestIcon = Favicon("http://img.zeit.de/static/img/zo-icon-win8-144x144.png", FaviconType.MsTileImage)
        val favicons = listOf<Favicon>(Favicon("http://www.zeit.de/favicon.ico", FaviconType.ShortcutIcon, Size(32, 32)),
                Favicon("http://img.zeit.de/static/img/ZO-ipad-114x114.png", FaviconType.AppleTouchPrecomposed),
                bestIcon)

        val result = underTest.getBestIcon(favicons)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForDieZeitIncludingRssIcon() {
        val bestIcon = Favicon("http://img.zeit.de/bilder/elemente_01_06/logos/homepage_top.gif", FaviconType.Icon)
        val favicons = listOf<Favicon>(Favicon("http://img.zeit.de/static/img/zo-icon-win8-144x144.png", FaviconType.MsTileImage),
                Favicon("http://www.zeit.de/favicon.ico", FaviconType.ShortcutIcon, Size(32, 32)),
                Favicon("http://img.zeit.de/static/img/ZO-ipad-114x114.png", FaviconType.AppleTouchPrecomposed),
                bestIcon)

        val result = underTest.getBestIcon(favicons)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestSquareIconForDieZeitIncludingRssIcon() {
        val bestIcon = Favicon("http://img.zeit.de/static/img/zo-icon-win8-144x144.png", FaviconType.MsTileImage)
        val favicons = listOf<Favicon>(Favicon("http://img.zeit.de/bilder/elemente_01_06/logos/homepage_top.gif", FaviconType.Icon),
                Favicon("http://www.zeit.de/favicon.ico", FaviconType.ShortcutIcon, Size(32, 32)),
                Favicon("http://img.zeit.de/static/img/ZO-ipad-114x114.png", FaviconType.AppleTouchPrecomposed),
                bestIcon)

        val result = underTest.getBestIcon(favicons, returnSquarishOneIfPossible = true)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForHeise() {
        val bestIcon = Favicon("http://www.heise.de/icons/ho/heise_online_facebook_social_graph.png", FaviconType.OpenGraphImage)
        val favicons = listOf<Favicon>(Favicon("http://www.heise.de/icons/ho/apple-touch-icon-152.png", FaviconType.AppleTouchPrecomposed, Size(152, 152)),
                Favicon("http://www.heise.de/favicon.ico", FaviconType.Icon),
                Favicon("http://www.heise.de/icons/ho/apple-touch-icon-60.png", FaviconType.AppleTouch, Size(60, 60)),
                Favicon("http://www.heise.de/icons/ho/apple-touch-icon-120.png", FaviconType.AppleTouch, Size(120, 120)),
                Favicon("http://www.heise.de/icons/ho/apple-touch-icon-76.png", FaviconType.AppleTouch, Size(76, 76)),
                bestIcon)

        val result = underTest.getBestIcon(favicons)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForPostillon() {
        val bestIcon = Favicon("http://4.bp.blogspot.com/-46xU6sntzl4/UVHLh1NGfwI/AAAAAAAAUlY/RiARs4-toWk/s800/Logo.jpg", FaviconType.OpenGraphImage)
        val favicons = listOf<Favicon>(Favicon("http://www.der-postillon.com/favicon.ico", FaviconType.Icon, type = "image/x-icon"), bestIcon)

        val result = underTest.getBestIcon(favicons)
        assertThat(result, `is`(bestIcon))
    }

    @Test
    fun getBestIconForPostillon_NoSquarishIconAvailable_ReturnsIconWithBestSizeThen() {
        val bestIcon = Favicon("http://4.bp.blogspot.com/-46xU6sntzl4/UVHLh1NGfwI/AAAAAAAAUlY/RiARs4-toWk/s800/Logo.jpg", FaviconType.OpenGraphImage)
        val favicons = listOf<Favicon>(Favicon("http://www.der-postillon.com/favicon.ico", FaviconType.Icon, type = "image/x-icon"), bestIcon)

        val result = underTest.getBestIcon(favicons, returnSquarishOneIfPossible = true)
        assertThat(result, `is`(bestIcon))
    }

}