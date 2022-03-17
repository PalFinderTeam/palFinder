package com.github.palFinderTeam.palfinder.utils.images

import com.github.palFinderTeam.palfinder.utils.image.UrlFormat
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.IllegalArgumentException

class UrlFormatTest {

    @Test
    fun webUrlIsRecognized() {
        Assert.assertEquals(UrlFormat.getUrlType("https://example.com/pics/1.jpg"), UrlFormat.URL_IS_WEB)
    }

    @Test
    fun fireBaseUrlIsRecognized() {
        Assert.assertEquals(UrlFormat.getUrlType("foo/bar.ext"), UrlFormat.URL_IS_FIREBASE)
    }

    @Test
    fun urlExtensionIsCorrectlyParsed() {
        Assert.assertEquals(UrlFormat.getUrlExtension("foo/bar.ext"), "ext")
        Assert.assertEquals(UrlFormat.getUrlExtension("https://example.com/pics/1.jpg"), "jpg")
        Assert.assertEquals(UrlFormat.getUrlExtension("http://example.com/pics/2.jpg"),  "jpg")
    }

    @Test
    fun urlExtensionParseThrowsExceptionIfNoExtension() {
        assertThrows(IllegalArgumentException::class.java) {
            UrlFormat.getUrlExtension("not/aLink")
        }
    }

}