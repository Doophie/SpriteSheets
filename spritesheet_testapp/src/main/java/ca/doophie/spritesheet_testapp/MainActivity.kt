package ca.doophie.spritesheet_testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.doophie.spritesheets.extensions.bitmap
import ca.doophie.spritesheets.spriteSheet.SpriteSheetSpriteBuilder
import ca.doophie.spritesheets.spriteSheet.SpriteSheetTicker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainSpriteSheet.ticker = SpriteSheetTicker("Main")

        val characterSprite = SpriteSheetSpriteBuilder("spritedude")
            .setWidth(200f)
            .setHeight(200f)
            .setNumRows(4)
            .setFrameCount(4)
            .build(this)

        playerJoystick.movementCallbacks = characterSprite

        characterSprite.x = 50
        characterSprite.y = 50
        characterSprite.maxFPS = 6

        mainSpriteSheet.playerCharacterSprite = characterSprite

        mainSpriteSheet.ticker?.resume()
    }

}