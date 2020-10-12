package ca.doophie.spritesheets.spriteSheet

import android.util.Log

interface TickSubscriber {
    fun tick(): Boolean
}

class SpriteSheetTicker(private val threadName: String): Runnable {

    companion object {
        const val TAG = "SpriteSheetTicker"
    }

    /**
     * Private vars
     */

    private var tickerThread: Thread? = null
    private var listeners: List<TickSubscriber> = emptyList()

    private var lastFrameChangeTime: Long = 0
    private val frameLengthInMilliseconds = 2

    var isRunning: Boolean = false
        private set

    private var pastFps: List<Long> = emptyList()
    var fps: Long = 60
        private set(value) {
            val pastFps = pastFps.toCollection(ArrayList())

            if (value != 0L) pastFps.add(value)

            field = if (pastFps.count() == 0)
                1
            else
                pastFps.sum() / pastFps.count()

            if (pastFps.count() > 5) pastFps.drop(1)

            this.pastFps = pastFps
        }

    /**
     * Public methods
     */

    fun pause() {
        isRunning = false
        try {
            tickerThread?.join()
            tickerThread = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Joining thread: ${e.localizedMessage}")
        }
    }

    fun resume() {
        if (isRunning) return

        isRunning = true

        if (tickerThread == null) {
            tickerThread = Thread(this, threadName)
        }

        tickerThread?.start()
    }

    override fun run() {
        var lastFrameTime = System.currentTimeMillis()
        while (isRunning) {
            if (tick()) {
                val timeThisFrame = System.currentTimeMillis() - lastFrameTime

                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame
                }

                lastFrameTime = System.currentTimeMillis()
            }
        }
    }

    fun subscribe(listener: TickSubscriber) {
        val updatedListeners = ArrayList<TickSubscriber>()

        updatedListeners.addAll(listeners)
        updatedListeners.add(listener)

        listeners = updatedListeners
    }

    fun subscribe(listener: ()->Unit) {
        subscribe(object : TickSubscriber {
            override fun tick(): Boolean {
                listener()

                return true
            }
        })
    }

    private fun tick(): Boolean {
        if (!getCurrentFrame()) return false

        var success = true

        listeners.forEach { success = it.tick() }

        return success
    }

    private fun getCurrentFrame(): Boolean {
        val time = System.currentTimeMillis()
        if (isRunning) {// Only animate if bob is moving
            if (time > lastFrameChangeTime + frameLengthInMilliseconds) {
                lastFrameChangeTime = System.currentTimeMillis()

                return true
            }
        }

        return false
    }


}