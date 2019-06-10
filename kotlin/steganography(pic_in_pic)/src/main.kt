import SteganographyPic.Companion.embedMessage
import SteganographyPic.Companion.generateImage
import SteganographyPic.Companion.retrieveEncodedMessageFromImage
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
    try {
        print("Enter the location of the image you wish to embed: ")
        val embeddedImagPath = readLine()!!
        println()
        print("Enter the location of the image to be encoded: ")
        val originalImgPath = readLine()!!
        println()
        print("Enter the location where you wish to save the new encoded image: ")
        val newImgPath = readLine()!!
        println()
        println("Encoding Image...")

        val newBufferedImg = embedMessage(ImageIO.read(File(originalImgPath)), ImageIO.read(File(embeddedImagPath)))
        println("Encoded Image Saved At: ${generateImage(newBufferedImg, newImgPath)}")
        println("Image encoded successfully!")
    }
    catch (e: SteganographyPic.Companion.OriginalPicTooSmallException) {
        println(e.errMsg)
    }
    /*catch (e: Exception) {
        println(e)
        println("Something went wrong, please try again later.")
    }*/
}

fun decode() {
    print("Enter the location of the encoded image: ")
    val picPath = readLine()!!
    println()
    print("Enter the location where you wish to save the new retrieved image: ")
    val retrievedImagePath = readLine()!!
    println()
    println("Retrieving Image...")

    val retrievedImage = retrieveEncodedMessageFromImage(ImageIO.read(File(picPath)))
    println("Retrieved Image Saved At: ${generateImage(retrievedImage, retrievedImagePath)}")
}

private class WrongMenuChoiceException : Exception() {
    override fun toString(): String {
        return this.javaClass.canonicalName + ": Invalid Choice!"
    }
}


