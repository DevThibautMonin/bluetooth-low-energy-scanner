package com.example.bluetoothlowenergyscanner.utils

import com.example.bluetoothlowenergyscanner.R

class Utils {
    companion object {
        fun getLogoByManufacturerId(manufacturerId: Int): Int? {
            return when (manufacturerId) {
                6 -> R.drawable.logo_microsoft
                76 -> R.drawable.logo_apple
                117 -> R.drawable.logo_samsung
                224 -> R.drawable.logo_google
                else -> null
            }
        }
    }
}
