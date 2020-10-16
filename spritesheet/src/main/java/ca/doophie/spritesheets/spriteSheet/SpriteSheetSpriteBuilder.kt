package ca.doophie.spritesheets.spriteSheet

import android.app.Activity
import ca.doophie.spritesheets.extensions.bitmap

class SpriteSheetSpriteBuilder(private val resourceName: String) {
    // params
    private var frameWidth: Float = 50f
    private var frameHeight: Float = 50f
    private var frameCount: Int = 1
    private var elevation: Int = 1
    private var heightMultiple: Int = 1
    private var heightRow: Int = 0
    private var skipLastFrames: Int = 0
    private var skipFirstFrames: Int = 0

    // other variables to set
    private var initialX: Int? = null
    private var initialY: Int? = null

    private var moveSpeed: Int? = null
    private var animationFPS: Int? = null

    // set row to draw
    var rowToDrawForDirection = hashMapOf(
        Direction.DOWN to 0,
        Direction.UP to 0,
        Direction.LEFT to 0,
        Direction.RIGHT to 0
    )

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
        for (direction in Direction.values()) {
            rowToDrawForDirection[direction] = row
        }
        return this
    }

    fun setRowToDrawForDirection(row: Int, direction: Direction): SpriteSheetSpriteBuilder {
        rowToDrawForDirection[direction] = row
        return this
    }

    fun setLocation(x: Int, y: Int): SpriteSheetSpriteBuilder {
        initialX = x
        initialY = y

        return this
    }

    fun setSkipFrames(front: Int, back: Int): SpriteSheetSpriteBuilder {
        this.skipFirstFrames = front
        this.skipLastFrames = back

        return this
    }

    fun setAnimationFPS(fps: Int): SpriteSheetSpriteBuilder {
        animationFPS = fps
        return this
    }

    fun setRunSpeed(speed: Int): SpriteSheetSpriteBuilder {
        moveSpeed = speed
        return this
    }

    fun build(activity: Activity): SpriteSheetSprite {
        val sprite = SpriteSheetSprite(
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

        sprite.rowToDrawForDirection = rowToDrawForDirection

        moveSpeed?.let { sprite.movespeed = it }
        animationFPS?.let { sprite.maxFPS = it }

        if (initialX != null && initialY != null) {
            sprite.x = initialX!!
            sprite.y = initialY!!
        }

        return sprite
    }
}