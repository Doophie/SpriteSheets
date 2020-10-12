package ca.doophie.spritesheets.spriteSheet

import android.graphics.PointF

interface SpriteSheetDisplayable {
    val elevation: Int
    var lastFrame: FrameInfo?
    fun getFrameInfo(fps: Int, xyOffset: PointF = PointF(-1f, -1f)): FrameInfo
}