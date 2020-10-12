package ca.doophie.spritesheets.extensions

import android.graphics.Rect

val Rect.list: List<Int>
    get() = listOf(top, left, right, bottom)