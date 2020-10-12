package ca.doophie.spritesheets.spriteSheet

import android.app.Activity
import ca.doophie.spritesheets.extensions.bitmap

class SpriteSheetSpriteBuilder(private val resourceName: String) {
    private var frameWidth: Float = 50f
    private var frameHeight: Float = 50f
    private var frameCount: Int = 1
    private var elevation: Int = 1
    private var heightMultiple: Int = 1
    private var heightRow: Int = 0
    private var skipLastFrames: Int = 0
    private var skipFirstFrames: Int = 0

    fun setWidth(width: Float): SpriteSheetSpriteBuilder {
        frameWidth = width
        return this
    }

    fun setHeight(height: Float): SpriteSheetSpriteBuilder {
        frameHeight = height
        return this
    }

    fun setFrameCount(count: Int): SpriteSheetSpriteBuilder {
        frameCount = count
        return this
    }

    fun setElevation(elevation: Int): SpriteSheetSpriteBuilder {
        this.elevation = elevation
        return this
    }

    fun setNumRows(rows: Int): SpriteSheetSpriteBuilder {
        heightMultiple = rows
        return this
    }

    fun setRowToDraw(row: Int): SpriteSheetSpriteBuilder {
        heightRow = row
        return this
    }

    fun setSkipFrames(front: Int, back: Int): SpriteSheetSpriteBuilder {
        this.skipFirstFrames = front
        this.skipLastFrames = back

        return this
    }

    fun build(activity: Activity): SpriteSheetSprite {
        return SpriteSheetSprite(
            resourceName,
            activity.resources.bitmap(activity.resources.getIdentifier(resourceName, "drawable", activity.packageName))!!,
            frameWidth,
            frameHeight,
            frameCount,
            elevation,
            heightMultiple,
            heightRow,
            skipLastFrames,
            skipFirstFrames
        )
    }
}