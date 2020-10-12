# Sprite Sheets

### About

This Library is meant for making simple 2d sprite sheet animations. It also includes a joystick view
which is integrated with the sprites.

### Installation

Add this to your project gradle file repositories:

```groovy
repositories {
    maven {
        url  "https://dl.bintray.com/doophie/SpriteSheet" 
    }
}
```

And add this to your app level implementations: 
```groovy
implementation 'ca.doophie:spritesheet:0.0.1'
```

### Usage

Take a look at the test app for further usage guidance, a basic implementation would be:

Include a sprite sheet via xml:
```xml
    <ca.doophie.spritesheets.spriteSheet.SpriteSheet
        android:id="@+id/mainSpriteSheet"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

And in Kotlin add a sprite and run the sheet like so:

```kotlin
// A sprite sheet ticker is used to resume and pause the animations, it will run the thread that
// all sprite sheet actions with be run on.
mainSpriteSheet.ticker = SpriteSheetTicker("Main")

// Use a builder to create a sprite sheet - you will need the sprite sheet resource
// in your drawables folder, and the name of that resource should be supplied as the 
// first parameter. The resource will need to be a grid of sprites with each rectangular 
// sprite being the having the same height / width of each other. Animations will draw the sprite
// from left to right on the specified row.
val characterSprite = SpriteSheetSpriteBuilder("spritedude")
    .setWidth(200f)
    .setHeight(200f)
    .setNumRows(4)
    .setFrameCount(4)
    .build(this)

// Add the sprite to the sprite sheet - The player character sprite has special properties,
// such as adding a camera to pan the view with the movement of it. 
mainSpriteSheet.playerCharacterSprite = characterSprite

// Call resume on the ticker to begin animations.
mainSpriteSheet.ticker?.resume()
```