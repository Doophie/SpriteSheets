package ca.doophie.spritesheets.spriteSheet

import android.graphics.*
import ca.doophie.spritesheets.extensions.*
import ca.doophie.spritesheets.views.JoystickMovementCallbacks
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

enum class MovementPattern {
    BOUND_BOUNCE;
}

enum class Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN;

    val i: Int
        get() = when(this) {
            LEFT -> 0
            RIGHT -> 2
            UP -> 1
            DOWN -> 3
        }
}

data class FrameInfo (
    val bitmap: Bitmap,
    val frameToDraw: Rect,
    var location: RectF
)

class SpriteSheetSprite(val bitmapName: String,
                        private val image: Bitmap,
                        val frameWidth: Float,
                        val frameHeight: Float,
                        val frameCount: Int,
                        override var elevation: Int = 1,
                        val heightMultiple: Int = 1,
                        val heightRow: Int = 0,
                        val skipLastFrames: Int = 0,
                        val skipFirstFrames: Int = 0,
                        val uuid: UUID = UUID.randomUUID()) : JoystickMovementCallbacks,
    SpriteSheetDisplayable {

    val totalFrames
        get() = frameCount + skipFirstFrames + skipLastFrames

    private fun copy(bitmapName: String = this.bitmapName,
                     image: Bitmap = this.image,
                     frameWidth: Float = this.frameWidth,
                     frameHeight: Float = this.frameHeight,
                     frameCount: Int = this.frameCount,
                     elevation: Int = this.elevation,
                     heightMultiple: Int = this.heightMultiple,
                     heightRow: Int = this.heightRow,
                     skipLastFrames: Int = this.skipLastFrames,
                     skipFirstFrames: Int = this.skipFirstFrames): SpriteSheetSprite {

        val newSkipLast = if (frameCount == 1) {
            skipLastFrames + (this.skipFirstFrames - skipFirstFrames)
        } else skipLastFrames

        return SpriteSheetSprite(bitmapName, image, frameWidth, frameHeight, frameCount, elevation, heightMultiple, heightRow, newSkipLast, skipFirstFrames)
    }

    val icon: Bitmap
        get() {
            this.copy(frameCount = 1, skipLastFrames = skipLastFrames + frameCount - 1).getFrameInfo(50).let {
                return it.bitmap.cropped(it.frameToDraw)
            }
        }

    var movespeed = 10


    private fun getRampWall(side: Direction, rampLocation: RectF): RectF {
        val wallThickness = 20
        val wallOffset = 0

        return when (side) {
            Direction.LEFT -> RectF(rampLocation.left - wallThickness, rampLocation.top, rampLocation.left, rampLocation.bottom)
            Direction.RIGHT -> RectF(rampLocation.right, rampLocation.top, rampLocation.right + wallOffset, rampLocation.bottom)
            Direction.DOWN -> RectF(rampLocation.left, rampLocation.bottom, rampLocation.left, rampLocation.bottom + wallThickness)
            Direction.UP -> RectF(rampLocation.left, rampLocation.top - wallThickness, rampLocation.left, rampLocation.top)
        }
    }

    var triggers: List<SpecialPoint> = emptyList()
        set(value) {
            val updatedList = value.toCollection(ArrayList())

            // add walls on the side of the ramps
            value.filter { it.type == BackgroundInteractable.RAMP }.forEach {
                val walledSides = when (it.upDirection) {
                    Direction.LEFT -> listOf(Direction.LEFT, Direction.UP, Direction.DOWN)
                    Direction.RIGHT -> listOf(Direction.RIGHT, Direction.UP, Direction.DOWN)
                    Direction.UP -> listOf(Direction.UP, Direction.LEFT, Direction.RIGHT)
                    else -> listOf(Direction.DOWN, Direction.LEFT, Direction.RIGHT)
                }

                // elevation for wall on opposite side is ramps floor
                for ((i, side) in walledSides.withIndex()) {
                    updatedList.add(
                        SpecialPoint(getRampWall(side, it.location),
                                     BackgroundInteractable.WALL,
                                     if (i == 0) it.elevation else -1)
                    )
                }
            }

            field = updatedList
        }

    var bounds = Rect(0,0,3000, 3000)
    var x = 1500
    var y = 1500
    var direction: Pair<Direction, Direction> = Pair(Direction.RIGHT, Direction.DOWN)

    var autoMovePattern: MovementPattern? = null
        set(value) {
            if (value != null) s = 0.8
            field = value
        }

    var s = 0.0
    var a = 0.0

    // the most amount of charater frames to display
    var maxFPS = frameCount / 2
    var framesSinceLastChange = 0
    var currentFrame = 0

    private val frameToDraw = Rect(0, 0, frameWidth.toInt(), frameHeight.toInt())

    private var _flipped: Bitmap? = null
    private val flipped: Bitmap
        get() {
            if (_flipped == null)
                _flipped = bitmap.flipped

            return _flipped!!
        }

    // Declare an object of type Bitmap
    private var _bitmap: Bitmap? = null
    private val bitmap: Bitmap
        get() {
            if (_bitmap != null) return _bitmap!!

            _bitmap = Bitmap.createScaledBitmap(
                image,
                (frameWidth * (frameCount + skipLastFrames + skipFirstFrames)).toInt(),
                (frameHeight * heightMultiple).toInt(),
                false
            )

            return _bitmap!!
        }

    private fun handleTriggerCollisions(destination: Point, character: RectF): Point {
        var finalX = destination.x
        var finalY = destination.y

        /*
            Handle special things we might be hitting
         */
        for (point in triggers.filter {
            it.type == BackgroundInteractable.WALL && (it.elevation == elevation || it.elevation == -1)
        }) {
            val wall = point.location

            val overlaps = character.overlaps(wall)

            // if we hit a wall, set our y to be against that wall
            if (overlaps.bottom == 1) finalY = wall.top.toInt() - frameHeight.toInt() + 2
            if (overlaps.top == 1) finalY = wall.bottom.toInt() - 2
            if (overlaps.right == 1) finalX = wall.left.toInt() - frameWidth.toInt() - 2
            if (overlaps.left == 1) finalX = wall.right.toInt() + 2
        }

        for (ramp in triggers.filter {
            it.type == BackgroundInteractable.RAMP &&
            (it.elevation == elevation || it.rampCeiling == elevation)
        }) {
            val rHits = ramp.location.overlaps(character).list
            if (rHits == listOf(0,0,0,0)) continue

            val baseSide = (ramp.upDirection.i + 2) % 4

            elevation = if (direction.first == ramp.upDirection || direction.second == ramp.upDirection) {
                // heading up
                if (rHits[baseSide] == 1)
                    ramp.rampCeiling
                else
                    elevation
            } else {
                if (rHits[baseSide] == 1)
                    ramp.elevation
                else
                    elevation
            }
        }

        for (specialEffect in triggers.filter { it.type == BackgroundInteractable.SPECIAL_EFFECT && it.elevation == elevation }) {
            if (specialEffect.location.overlaps(character).list != listOf(0,0,0,0)) {
                // TODO: Trigger special effect
            }
        }

        return Point(finalX, finalY)
    }

    private fun updateSpriteLocation(fps: Int): Point {
        val strength = s * movespeed * (80/max(fps.toDouble(),1.0))
        val xDelta =  (strength * cos(Math.toRadians(a))).toInt()
        val yDelta = (strength * sin(Math.toRadians(a))).toInt()

        direction = direction.copy(
            first = if (xDelta < 0) Direction.LEFT else if (xDelta > 0) Direction.RIGHT else direction.first
        )

        val character = RectF((x+xDelta).toFloat(), (y-yDelta).toFloat(), x+xDelta+frameWidth, y-yDelta+frameHeight)

        val finalPos = handleTriggerCollisions(Point(x + xDelta, y - yDelta), character)

        if (autoMovePattern != null) {
            // if we hit a wall change directions
            if (a == 0.0 ||
                x == finalPos.x ||
                y == finalPos.y)
                a = (0 until 360).random().toDouble()
        }

        x = finalPos.x
        y = finalPos.y

        val elevatedPlatform = triggers.firstOrNull {
            it.elevation == elevation && it.type == BackgroundInteractable.ELEVATED_SPACE
        }

        val ramps =  triggers.filter {
            it.rampCeiling == elevation && it.type == BackgroundInteractable.RAMP
        }

        var bounds = elevatedPlatform?.location?.rect ?: bounds

        bounds = Rect(
            (ramps.firstOrNull { it.upDirection == Direction.RIGHT }?.location?.left?.toInt() ?: bounds.left) - 3,
            (ramps.firstOrNull { it.upDirection == Direction.DOWN }?.location?.top?.toInt() ?: bounds.top) - 3,
            (ramps.firstOrNull { it.upDirection == Direction.LEFT }?.location?.right?.toInt() ?: bounds.right) + 3,
            (ramps.firstOrNull { it.upDirection == Direction.UP }?.location?.bottom?.toInt() ?: bounds.bottom) + 3
        )

        x = maxOf(bounds.left, minOf(bounds.right - frameWidth.toInt(), x))
        y = maxOf(bounds.top, minOf(bounds.bottom - frameHeight.toInt(), y))

        // Move auto moving bots
        if (autoMovePattern != null) {
            // if we hit a wall change directions
            if (a == 0.0 ||
                x == bounds.right - frameWidth.toInt() ||
                y == bounds.bottom - frameHeight.toInt() ||
                x == bounds.left ||
                y == bounds.right)
                a = (0 until 360).random().toDouble()
        }

        return Point()
    }

    override var lastFrame: FrameInfo? = null
    override fun getFrameInfo(fps: Int, xyOffset: PointF): FrameInfo {
        if (isAnimating) return getAnimationFrame(fps)

        val framesPerUpdate = if (maxFPS > 0) fps/maxFPS else 0

        updateSpriteLocation(fps)

        // player location changes dramatically by camera offset
        val drawnX = if (xyOffset.x == -1f) x else xyOffset.x.toInt()
        val drawnY = if (xyOffset.y == -1f) y else xyOffset.y.toInt()

        val location = RectF(drawnX.toFloat(), drawnY.toFloat(),
            frameWidth + drawnX, frameHeight + drawnY)

        framesSinceLastChange = if (framesPerUpdate > 0) {
            (framesSinceLastChange + 1) % framesPerUpdate
        } else
            1

        if (framesSinceLastChange + 1 < framesPerUpdate && lastFrame != null) {
            lastFrame?.location = location
            return lastFrame!!
        }

        // flip if facing left
        val bmp = if (direction.first == Direction.LEFT) flipped else bitmap

        val frameIndex = frameCount - skipLastFrames
        currentFrame = if (frameIndex > 0)
            (currentFrame + skipFirstFrames + 1) % frameIndex
        else
            skipFirstFrames

        // frames backwards if facing left
        val frame = if (direction.first == Direction.LEFT)
            frameCount - currentFrame
        else currentFrame

        frameToDraw.left = (frame * frameWidth).toInt()
        frameToDraw.right = (frameToDraw.left + frameWidth).toInt()

        frameToDraw.top = (frameHeight * heightRow).toInt()
        frameToDraw.bottom = (frameToDraw.top + frameHeight).toInt()

        lastFrame = FrameInfo(bmp, frameToDraw, location)

        return lastFrame!!
    }

    fun move(strength: Double, angle: Double) {
        s = strength
        a = angle
    }

    override fun onMove(strength: Double, angle: Double) {
        move(strength, angle)
    }

    override fun equals(other: Any?): Boolean {
        return (other as SpriteSheetSprite?)?.uuid == uuid
    }

    /***
     * Handling animations below this point
     */

    var isAnimating: Boolean = false
    private var destination: Point = Point(0, 0)
    private var initialPoint = Point(x, y)
    private var traveresedPoints = ArrayList<Point>()
    private var animationIndex = 0
    private var framesToAnimate = 70
    private var animationCallback: ()->Unit = {}
    private var animationType = AnimationType.BOOMERANG_REPEAT

    private fun getAnimationFrame(fps: Int): FrameInfo {
        if (animationIndex  > framesToAnimate && lastFrame != null)
            return lastFrame!!

        val framesPerUpdate = if (animationType == AnimationType.ONEWAY_SINGLE_LOOP)
            framesToAnimate / frameCount
        else
            if (maxFPS > 0) fps/maxFPS else 0

        framesSinceLastChange += 1

        if (framesSinceLastChange > framesPerUpdate || lastFrame == null) {
            framesSinceLastChange = 0

            currentFrame = if (animationType == AnimationType.ONEWAY_SINGLE_LOOP)
                minOf(currentFrame + 1, frameCount + skipFirstFrames - 1)
            else
                ((currentFrame + 1) % frameCount) + skipFirstFrames

            frameToDraw.left = (currentFrame * frameWidth).toInt()
            frameToDraw.right = (frameToDraw.left + frameWidth).toInt()

            frameToDraw.top = (frameHeight * heightRow).toInt()
            frameToDraw.bottom = (frameToDraw.top + frameHeight).toInt()
        }

        lastFrame = FrameInfo(
            bitmap,
            frameToDraw,
            nextAnimationLocation()
        )

        return lastFrame!!
    }

    private fun nextAnimationLocation(): RectF {
        if (traveresedPoints.count() > framesToAnimate/2 &&
            !(animationType == AnimationType.ONEWAY_REPEAT ||
             animationType == AnimationType.ONEWAY_SINGLE_LOOP)) {

            animationIndex -= 1

            val point = if (animationIndex <= -1) {
                isAnimating = false
                x = initialPoint.x
                y = initialPoint.y

                val callback = animationCallback

                animationCallback = {}

                callback()

                initialPoint
            } else {
                traveresedPoints[animationIndex]
            }

            return RectF(point.x.toFloat(), point.y.toFloat(), point.x + frameWidth, point.y + frameHeight)
        }

        animationIndex += 1

        val pointDistance = sqrt((destination.x - initialPoint.x.toDouble()).pow(2) + (destination.y - initialPoint.y.toDouble()).pow(2))

        val distanceRatio = 15 / pointDistance

        x += (distanceRatio * (destination.x - initialPoint.x)).toInt()
        y += (distanceRatio * (destination.y - initialPoint.y)).toInt()

        traveresedPoints.add(Point(x, y))

        if (traveresedPoints.count() >= framesToAnimate) {
            isAnimating = false || animationType == AnimationType.ONEWAY_SINGLE_LOOP
            animationCallback()
            animationCallback = {}
        }

        return RectF(x.toFloat(), y.toFloat(),
            x + frameWidth, y + frameHeight)
    }

    fun animate(location: Point, type: AnimationType, onComplete: ()->Unit = {}) {
        animationIndex = 0
        if (type == AnimationType.ONEWAY_SINGLE_LOOP) currentFrame = skipFirstFrames
        traveresedPoints = ArrayList()
        destination = location
        initialPoint = Point(x, y)
        animationCallback = onComplete
        isAnimating = true
        animationType = type
    }

    enum class AnimationType {
        BOOMERANG_REPEAT,
        ONEWAY_SINGLE_LOOP,
        ONEWAY_REPEAT;
    }

}