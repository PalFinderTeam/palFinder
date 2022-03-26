package com.github.palFinderTeam.palfinder.utils.image

import java.lang.IllegalArgumentException

/**
 * Static class, helper functions to handle URLs such as finding the
 * type of URL, the extension of a file at a URL
 */
class UrlFormat {

    companion object{
        // Used as image types
        const val URL_IS_FIREBASE = "firebase"
        const val URL_IS_WEB = "web"

        private const val WEB_PATH_HTTPS : String = "https://"
        private const val WEB_PATH_HTTP  : String = "http://"

        /**
         * If function starts with `https://` then is is considered
         * to be a web link, otherwise it will be considered as Firebase
         * @param url URL of the file
         */
        fun getUrlType(url: String) : String {
            return if(url.startsWith(WEB_PATH_HTTPS, true)
                || url.startsWith(WEB_PATH_HTTP, true)) {
                URL_IS_WEB
            } else {
                URL_IS_FIREBASE
            }
        }

        /**
         * Get the extension of the url to the file, to get the
         * extension of the file itself
         * @param url URL of the file
         */
        fun getUrlExtension(url: String) : String{
            val s = url.split(".")
            if (s.size <= 1) throw IllegalArgumentException("URL '$url' does not contain an extension")
            return s[s.size-1]
        }
    }

}