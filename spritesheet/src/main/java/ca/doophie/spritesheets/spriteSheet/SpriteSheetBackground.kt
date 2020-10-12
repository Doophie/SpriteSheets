package ca.doophie.spritesheets.spriteSheet

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF

enum class BackgroundInteractable {
    EXIT,
    WALL,
    RAMP,
    CHEST,
    SPECIAL_EFFECT,
    ELEVATED_SPACE;
}

data class SpecialPoint(
    val location: RectF,
    val type: BackgroundInteractable,
    val elevation: Int = 1,
    val upDirection: Direction = Direction.RIGHT,
    val rampCeiling: Int = 2
)

data class SpriteSheetBackground(
    val background: Bitmap,
    val initialPlayerLocation: PointF,
    val specialPoints: List<SpecialPoint>,
    val mask: Bitmap? = null
)