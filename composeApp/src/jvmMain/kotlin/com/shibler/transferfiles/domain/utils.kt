package com.shibler.transferfiles.domain

import java.util.Locale
import java.util.ResourceBundle

object Language{
    private var bundle = ResourceBundle.getBundle("text", Locale.getDefault())

    fun getString(key: String): String {
        return bundle.getString(key)
    }

    fun setLanguage(language: String) {
        bundle = ResourceBundle.getBundle("text", Locale(language))
    }
}