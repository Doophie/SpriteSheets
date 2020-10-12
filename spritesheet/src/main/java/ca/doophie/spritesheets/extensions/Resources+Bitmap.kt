package ca.doophie.spritesheets.extensions

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ca.doophie.spritesheets.R
import java.lang.Exception

fun Resources.bitmap(id: Int): Bitmap? {
    return BitmapFactory.decodeResource(this, id)
}

