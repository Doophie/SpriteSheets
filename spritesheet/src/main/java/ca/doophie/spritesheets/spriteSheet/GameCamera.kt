package ca.doophie.spritesheets.spriteSheet

import android.graphics.*
import android.util.Size

data class BackgroundFrameInfo (
    var bitmap: Bitmap,
    val frameToDraw: Rect,
    var frameDimens: RectF,
    val playerXYOffest: PointF
)

class BackgroundCamera(var screenDimens: Size,
                       var background: SpriteSheetBackground? = null) {

    private var lastFrameInfo: BackgroundFrameInfo? = null

    // Location is based on players location
    fun getDrawableBackground(playersLocation: PointF): BackgroundFrameInfo? {
        val background = background ?: return null

        // the border of the background which the camera cant  enter
        val cameraWallX = screenDimens.width/2
        val cameraWallY = screenDimens.height/2

        val playerXYOffest = PointF(cameraWallX.toFloat(), cameraWallY.toFloat())

        val frameToDraw = Rect()
        frameToDraw.bottom = (playersLocation.y + cameraWallY).toInt()
        frameToDraw.top = (playersLocation.y - cameraWallY).toInt()
        frameToDraw.left = (playersLocation.x - cameraWallX).toInt()
        frameToDraw.right = (playersLocation.x + cameraWallX).toInt()

        val frameDimens = RectF(
            0f, 0f, screenDimens.width.toFloat(), screenDimens.height.toFloat()
        )

        if (playersLocation.x < cameraWallX) {
            frameToDraw.left = 0
            frameToDraw.right = screenDimens.width
            playerXYOffest.x = playersLocation.x
        }

        if (playersLocation.x > background.background.width - cameraWallX) {
            frameToDraw.right = background.background.width
            frameToDraw.left = background.background.width - screenDimens.width
            playerXYOffest.x = screenDimens.width + (playersLocation.x - background.background.width)
        }

        if (playersLocation.y < cameraWallY) {
            frameToDraw.top = 0
            frameToDraw.bottom = screenDimens.height
            playerXYOffest.y = playersLocation.y
        }

        if (playersLocation.y > background.background.height - cameraWallY) {
            frameToDraw.top = background.background.height - screenDimens.height
            frameToDraw.bottom = background.background.height
            playerXYOffest.y = screenDimens.height + (playersLocation.y - background.background.height)
        }

        playerXYOffest.x = minOf(screenDimens.width.toFloat(), playerXYOffest.x)
        playerXYOffest.x = maxOf(0f, playerXYOffest.x)

        playerXYOffest.y = minOf(screenDimens.height.toFloat(), playerXYOffest.y)
        playerXYOffest.y = maxOf(0f, playerXYOffest.y)

        lastFrameInfo = BackgroundFrameInfo(background.background, frameToDraw, frameDimens, playerXYOffest)

        return lastFrameInfo
    }

    fun getDrawableMask(): BackgroundFrameInfo? {
        return background?.mask?.let { lastFrameInfo?.copy(bitmap = it) }
    }

}