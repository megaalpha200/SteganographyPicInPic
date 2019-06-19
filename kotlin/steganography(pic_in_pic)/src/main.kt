import SteganographyPic.Companion.embedMessage
import SteganographyPic.Companion.generateImage
import SteganographyPic.Companion.retrieveEncodedImageFromImage
import java.io.File
import java.lang.NumberFormatException
import javax.imageio.ImageIO

fun main() {

    do {
        try {
            println("Hello! Welcome to Image Encoder (Picture in Picture)!")
            println("Created by: Jose A. Alvarado")
            println("Copyright J.A.A. Productions 2019")
            println()
            println("Please select an option...")
            println("1. Encode")
            println("2. Decode")
            println("3. Quit")
            println()
            print("Choice: ")

            val userChoice = readLine()!!

            when (Integer.parseInt(userChoice)) {
                1 -> {
                    println()
                    encode()
                }
                2->{
                    println()
                    decode()
                }
                3-> {
                    println("Goodbye!")
                    return
                }
                else -> throw WrongMenuChoiceException()
            }

        }
        catch (e: NoSuchFileException) {
            println("Picture does not exist!")
        }
        catch (e: NumberFormatException) {
            println("Please enter a number from 1 to 3!")
        }
        catch (e: Exception) {
            println(e.toString())
        }

        println()
    } while (true)
}

fun encode() {
    print("Enter the location of the image you wish to embed: ")
    val embeddedImgPath = readLine()!!
    println()

    if (!checkIfFileExists(embeddedImgPath))
        throw NoSuchFileException(File(embeddedImgPath))

    print("Enter the location of the image to be encoded: ")
    val originalImgPath = readLine()!!
    println()

    if (!checkIfFileExists(originalImgPath))
        throw NoSuchFileException(File(originalImgPath))

    print("Enter the location where you wish to save the new encoded image: ")
    val newImgPath = readLine()!!
    println()
    println("Encoding Image...")

    val newBufferedImg = embedMessage(ImageIO.read(File(originalImgPath)), ImageIO.read(File(embeddedImgPath)))
    println("Encoded Image Saved At: ${generateImage(newBufferedImg, newImgPath)}")
    println("Image encoded successfully!")
}

fun decode() {
    print("Enter the location of the encoded image: ")
    val picPath = readLine()!!
    println()

    if (!checkIfFileExists(picPath))
        throw NoSuchFileException(File(picPath))

    print("Enter the location where you wish to save the new retrieved image: ")
    val retrievedImagePath = readLine()!!
    println()
    println("Retrieving Image...")

    val retrievedImage = retrieveEncodedImageFromImage(ImageIO.read(File(picPath)))
    println("Retrieved Image Saved At: ${generateImage(retrievedImage, retrievedImagePath)}")
    println("Image decoded successfully!")
}

private fun checkIfFileExists(filename: String): Boolean {
    return File(filename).exists()
}

private class WrongMenuChoiceException : Exception() {
    override fun toString(): String {
        return this.javaClass.canonicalName + ": Invalid Choice!"
    }
}


