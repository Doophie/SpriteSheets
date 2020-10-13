import cv2
import sys


def printUsage(self):
    print("Sprite Sheet Background Generator")
    print("---------------------------------")
    print("Creates a kotlin file containing an object which can be used to generate a SpriteSheetBackground")
    print("---------------------------------")
    print("Usage:")
    print("    - Requires params in the following order (* means optional):")
    print("      1 - The output file / class name")
    print("      2 - The file containing the actual background")
    print("      4 - The file containing rectangles outlining where walls should go")
    print("    * 3 - The file containing rectangles outlining where ramps should go")
    print("    * 5+ - Files containing elevated spaces, the later the file the higher the elevation")
    print("    * last - The final parameter can optionally be a multiplier for the desired size the drawn background, "
          "it will be x times bigger than the input.")


class SpriteSheetBackgroundGenerator:
    imageWidth = 0
    imageHeight = 0

    def getSpecialPoint(self, rect, type, multiplier, elevation=None):
        r = [(s + (rect[i - 2] if i > 1 else 0)) * multiplier for (i, s) in enumerate(rect)]

        if elevation is None:
            return f"SpecialPoint(RectF({r[0]}f, {r[1]}f, {r[2]}f, {r[3]}f), BackgroundInteractable.{type})"
        else:
            return f"SpecialPoint(RectF({r[0]}f, {r[1]}f, {r[2]}f, {r[3]}f), BackgroundInteractable.{type}, {elevation})"

    def getRects(self, filename):
        image = cv2.imread(filename)

        self.imageWidth = image.shape[1]
        self.imageHeight = image.shape[0]

        blur = cv2.pyrMeanShiftFiltering(image, 11, 21)
        gray = cv2.cvtColor(blur, cv2.COLOR_BGR2GRAY)
        thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

        cnts = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        cnts = cnts[0] if len(cnts) == 2 else cnts[1]
        rects = []
        for c in cnts:
            peri = cv2.arcLength(c, True)
            approx = cv2.approxPolyDP(c, 0.015 * peri, True)
            if len(approx) == 4:
                rects.append(cv2.boundingRect(approx))

        return rects

    def makePretty(self, points):
        endString = ""
        for point in points:
            endString += "                " + point + ",\n"

        return endString[:-2]


if __name__ == '__main__':
    if len(sys.argv) < 4:
        printUsage()
        quit()

    generator = SpriteSheetBackgroundGenerator()

    name = sys.argv[1]
    bgFile = sys.argv[2]
    wallsFile = sys.argv[3]
    rampsFile = None
    elevatedSpaces = None
    multiplier = 1

    if len(sys.argv) > 4:
        try:
            multiplier = int(sys.argv[-1])
            hasMult = True
        except Exception as e:
            hasMult = False

        if not (hasMult and len(sys.argv) == 5):
            rampsFile = sys.argv[4]

        if len(sys.argv) > 5:
            if hasMult:
                elevatedSpaces = sys.argv[5:-1]
            else:
                elevatedSpaces = sys.argv[5:]

    specialPoints = []

    for rect in generator.getRects(wallsFile):
        specialPoints.append(generator.getSpecialPoint(rect, "WALL", multiplier))

    if rampsFile is not None:
        for rect in generator.getRects(rampsFile):
            specialPoints.append(generator.getSpecialPoint(rect, "RAMP", multiplier))

    if elevatedSpaces is not None:
        for index, elevationFile in enumerate(elevatedSpaces):
            for rect in generator.getRects(elevationFile):
                specialPoints.append(generator.getSpecialPoint(rect, "ELEVATED_SPACE", multiplier, index + 2))

    print("package ca.doophie.spritesheet_testapp\n\n" +
          "import android.content.Context\n" +
          "import android.graphics.Bitmap\n" +
          "import android.graphics.PointF\n" +
          "import android.graphics.RectF\n" +
          "import ca.doophie.spritesheets.extensions.bitmap\n" +
          "import ca.doophie.spritesheets.spriteSheet.BackgroundInteractable\n" +
          "import ca.doophie.spritesheets.spriteSheet.SpecialPoint\n" +
          "import ca.doophie.spritesheets.spriteSheet.SpriteSheetBackground\n\n" +
          "object {} {{\n".format(name) +
          "    fun build(context: Context): SpriteSheetBackground {\n" +
          "        return SpriteSheetBackground(Bitmap.createScaledBitmap(context.resources.bitmap(R.drawable.{})!!, {}, {}, false),\n".format(
              bgFile.split("/")[-1].split(".")[0], generator.imageWidth * multiplier, generator.imageHeight * multiplier) +
          "            PointF(1f, 1f),\n" +
          "            listOf(\n" +
          "{}\n".format(generator.makePretty(specialPoints)) +
          "            ))\n" +
          "    }\n" +
          "} \n")
