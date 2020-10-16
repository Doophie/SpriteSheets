package ca.doophie.spritesheet_testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.doophie.spritesheets.extensions.bitmap
import ca.doophie.spritesheets.spriteSheet.Direction
import ca.doophie.spritesheets.spriteSheet.SpriteSheetSpriteBuilder
import ca.doophie.spritesheets.spriteSheet.SpriteSheetTicker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainSpriteSheet.ticker = SpriteSheetTicker("Main")

        mainSpriteSheet.background = TestBack.build(this)

        val characterSprite = SpriteSheetSpriteBuilder("spritedude")
            .setWidth(200f)
            .setHeight(200f)
            .setNumRows(4)
            .setFrameCount(4)
            .setRowToDrawForDirection(0, Direction.DOWN)
            .setRowToDrawForDirection(1, Direction.UP)
            .setRowToDrawForDirection(2, Direction.LEFT)
            .setRowToDrawForDirection(3, Direction.RIGHT)
            .setAnimationFPS(12)
            .setRunSpeed(25)
            .build(this)

        characterSprite.x = mainSpriteSheet.background!!.initialPlayerLocation.x.toInt()
        characterSprite.y = mainSpriteSheet.background!!.initialPlayerLocation.y.toInt()

        playerJoystick.movementCallbacks = characterSprite

        characterSprite.maxFPS = 6

        mainSpriteSheet.playerCharacterSprite = characterSprite

        mainSpriteSheet.ticker?.resume()
    }

}