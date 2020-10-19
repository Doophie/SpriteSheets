package ca.doophie.spritesheets.spriteSheet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.RectF.intersects
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import ca.doophie.spritesheets.ktextensions.rect
import ca.doophie.spritesheets.ktextensions.cropped
import java.lang.Exception

class SpriteSheet(context: Context, attrs: AttributeSet)
    : SurfaceView(context, attrs), TickSubscriber, SurfaceHolder.Callback {

    companion object {
        const val TAG = "SpriteSheet"
    }

    /**
     * Public vars to be set
     */

    var ticker: SpriteSheetTicker? = null
        set(value) {
            val ticker = value ?: return
            ticker.subscribe(this)
            field = value
        }

    var playerCharacterSprite: SpriteSheetSprite? = null
        set(value)  {
            if (value != null) {
                background?.let {
                    value.triggers = it.specialPoints
                    value.bounds = Rect(0, 0, it.background.width, it.background.height)
                }
            }

            field = value
        }
    var sprites: List<SpriteSheetDisplayable> = emptyList()
    var background: SpriteSheetBackground? = null
        set(value) {
            if (value != null) {
                playerCharacterSprite?.bounds = Rect(0, 0, value.background.width, value.background.height)
                playerCharacterSprite?.triggers = value.specialPoints

                setUpElevations(value)
            }

            field = value
        }

    // a simple background will be hidden if a regular background is set
    var simpleBackground: Bitmap? = null

    private var backgroundCamera: BackgroundCamera? = null

    /**
     * Private vars
     */

    private var canvas: Canvas? = null
    private var ourHolder: SurfaceHolder = holder
    private var paint: Paint = Paint()
    private var _onPlayerCollision: (SpriteSheetSprite)->Unit = {}

    /**
     * Public methods
     */

    fun onPlayerCollision(callback: (SpriteSheetSprite)->Unit) {
        _onPlayerCollision = callback
    }

    fun removeAllSprites() {
        sprites = emptyList()
    }

    fun addSprite(sprite: SpriteSheetDisplayable) {
        val updatedSprites = ArrayList<SpriteSheetDisplayable>()

        updatedSprites.addAll(sprites)

        background?.specialPoints?.let {
            (sprite as? SpriteSheetSprite?)?.triggers = it
        }

        updatedSprites.add(sprite)

        sprites = updatedSprites
    }

    fun removeSprite(sprite: SpriteSheetSprite) {
        val updatedSprites = ArrayList<SpriteSheetDisplayable>()

        updatedSprites.addAll(sprites)
        updatedSprites.remove(sprite)

        sprites = updatedSprites
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        backgroundCamera?.screenDimens = Size(measuredWidth, measuredHeight)
    }

    /**
     * Private methods
     */

    private fun setUpElevations(background: SpriteSheetBackground) {
        for (elevatedSpace in background.specialPoints.filter { it.elevation > 1 }) {
            addSprite(
                SpriteSheetBackgroundChunk(
                elevatedSpace.elevation,
                background.background.cropped(elevatedSpace.location.rect),
                elevatedSpace.location
            )
            )
        }
    }

    override fun tick(): Boolean {
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.surface.isValid) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas()

            var playerXYOffset = PointF(-1f, -1f)
            var cameraOffset = PointF()

            // Draw the background first
            if (background != null && measuredWidth > 0) {
                if (backgroundCamera == null)
                    backgroundCamera = BackgroundCamera(Size(measuredWidth, measuredHeight))

                backgroundCamera?.background = background

                playerCharacterSprite?.let { pc ->
                    backgroundCamera?.getDrawableBackground(PointF(pc.x.toFloat(), pc.y.toFloat()))
                        ?.let {
                            cameraOffset.x = it.frameToDraw.left.toFloat()
                            cameraOffset.y = it.frameToDraw.top.toFloat()

                            canvas?.drawBitmap(
                                it.bitmap,
                                it.frameToDraw,
                                it.frameDimens, paint
                            )

                            playerXYOffset = it.playerXYOffest
                        }
                }
            } else if (simpleBackground != null) {
                canvas?.drawBitmap(
                    simpleBackground!!,
                    0f,
                    0f, paint
                )
            } else
                canvas?.drawPaint(paint)

            // mix together so drawn by elevation
            val sprites = if (playerCharacterSprite != null)
                sprites + playerCharacterSprite!!
            else
                sprites

            // draw each sprite
            sprites.sortedBy { it.elevation }.forEach {
                if (it == playerCharacterSprite) {
                    val frameInfo = it.getFrameInfo(ticker?.fps?.toInt() ?: 1, playerXYOffset)
                    canvas?.drawBitmap(
                        frameInfo.bitmap,
                        frameInfo.frameToDraw,
                        frameInfo.location, paint
                    )
                } else {
                    val frameInfo = it.getFrameInfo(ticker?.fps?.toInt() ?: 1)
                    val viewableDimens = RectF(frameInfo.location)

                    // subtract from where camera is
                    viewableDimens.left -= cameraOffset.x
                    viewableDimens.right -= cameraOffset.x
                    viewableDimens.top -= cameraOffset.y
                    viewableDimens.bottom -= cameraOffset.y

                    canvas?.drawBitmap(
                        frameInfo.bitmap,
                        frameInfo.frameToDraw,
                        viewableDimens, paint
                    )

                    // check for intersection with player
                    if (it.elevation != playerCharacterSprite?.elevation) return@forEach // this return works like a continue
                    playerCharacterSprite?.lastFrame?.location?.let { ps ->
                        // re-add camera offset for checking intersection
                        val playerLocale = RectF(ps)
                        playerLocale.left += cameraOffset.x
                        playerLocale.right += cameraOffset.x
                        playerLocale.top += cameraOffset.y
                        playerLocale.bottom += cameraOffset.y

                        (it as? SpriteSheetSprite?)?.let { enemy ->
                            val actualDimens = RectF(enemy.lastFrame?.location)

                            if (intersects(playerLocale, actualDimens))
                                _onPlayerCollision(enemy)
                        }
                    }
                }
            }

            // Draw background mask
            backgroundCamera?.getDrawableMask()?.let {
                canvas?.drawBitmap(
                    it.bitmap,
                    it.frameToDraw,
                    it.frameDimens, paint
                )
            }

            // Draw everything to the screen
            try {
                ourHolder.unlockCanvasAndPost(canvas)
            } catch (e: Exception) {
                // can happen if canvas is released from quick run
                // or going to background
                Log.d("SpriteSheet", "Unable to draw canvas: ${e.localizedMessage}")
            }
        }

        return true
    }

    /*
    Surface holder callbacks
     */

    override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        invalidate()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        ticker?.pause()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        setWillNotDraw(false)
    }

    /*
    Special animations
     */

    /***
     * animates the given sprite, moves from from location to to location
     * if either location is null, sprite will remain stationary at whichever location is non-null
     * if both locations are null the sprite will remain at the sprite location
     * @param sprite -> the sprite to animate
     * @param fromLocation -> the location we are travelling from
     * @param toLocation -> the location we are travelling to
     * @param holdLastFrame -> how many seconds to keep the last frame
     * @param onComplete -> a callback for when the animation is over
     */
    fun doAnimation(sprite: SpriteSheetSprite,
                    fromLocation: Point? = null,
                    toLocation: Point? = null,
                    holdLastFrame: Int = 0,
                    onComplete: ()->Unit = {}) {

        // if the sheet already contains the sprite just move it
        if (sprites.contains(sprite)) {

            if (toLocation != null)
                sprite.animate(toLocation,
                    SpriteSheetSprite.AnimationType.BOOMERANG_REPEAT, onComplete)

        // otherwise add the new sprite and remove it when animation completes
        } else {
            addSprite(sprite)

            fromLocation?.let {

                sprite.x = it.x
                sprite.y = it.y

                if (toLocation != null)
                    sprite.animate(toLocation, SpriteSheetSprite.AnimationType.ONEWAY_SINGLE_LOOP) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            sprite.isAnimating = false
                            removeSprite(sprite)
                            onComplete()
                        }, holdLastFrame * 1000L)

                    }
            }

        }
    }


}
