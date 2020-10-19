package ca.doophie.spritesheet_testapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import ca.doophie.spritesheets.ktextensions.bitmap
import ca.doophie.spritesheets.spriteSheet.BackgroundInteractable
import ca.doophie.spritesheets.spriteSheet.SpecialPoint
import ca.doophie.spritesheets.spriteSheet.SpriteSheetBackground

/****************************************************
 *          This file was auto-generated            *
 *      using the generateBackgrounds.py script     *
 ****************************************************/
object TestBack {
    fun build(context: Context): SpriteSheetBackground {
        return SpriteSheetBackground(Bitmap.createScaledBitmap(context.resources.bitmap(R.drawable.background)!!, 9600, 5400, false),
            PointF(1f, 1f),
            listOf(
                SpecialPoint(RectF(7420f, 3600f, 8170f, 4510f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(850f, 2775f, 2005f, 3870f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(3445f, 2575f, 4020f, 3280f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(8895f, 1765f, 9600f, 5400f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(2215f, 1065f, 5120f, 2145f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(220f, 990f, 1345f, 1650f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(5155f, 450f, 6640f, 4720f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(7405f, 0f, 8190f, 2830f), BackgroundInteractable.WALL),
                SpecialPoint(RectF(2325f, 0f, 4440f, 460f), BackgroundInteractable.WALL)
            ))
    }
}
