package ca.doophie.spritesheets.spriteSheet

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF

class SpriteSheetBackgroundChunk(
    override val elevation: Int,
    val mapChunk: Bitmap,
    val location: RectF
) : SpriteSheetDisplayable {

    override var lastFrame: FrameInfo? = null
    override fun getFrameInfo(fps: Int, xyOffset: PointF): FrameInfo {
        lastFrame = FrameInfo(mapChunk,
            Rect(0,0,mapChunk.width,mapChunk.height),
            RectF(location))

        return lastFrame!!
    }

}