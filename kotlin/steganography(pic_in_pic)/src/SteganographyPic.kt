/*
* Created By: Jose A. Alvarado
* */

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.math.BigInteger
import javax.imageio.ImageIO

class SteganographyPic {
    companion object {
        private val bufferDelimiterList = listOf("1010", "1111", "0101")
        private val delimiterList = listOf("1111", "1010", "1111")
        private const val padding = "0000"
        private const val bufferGrouping = 3

        private fun convertMessageToBinary(name : String) : Pair<ArrayList<String>, Map<String, Char>> {
            val binaryNameArrayList = arrayListOf<String>()
            val charBinMap = mutableMapOf<String, Char>()

            println("Message: $name")
            print("Binary Representation: ")

            name.forEach {
                val currCharBinRepresentation = String.format("%08d", Integer.toBinaryString(it.toInt()).toInt())
                print("$currCharBinRepresentation ")
                charBinMap[currCharBinRepresentation] = it
                binaryNameArrayList.add(currCharBinRepresentation)
            }

            println()

            return Pair(binaryNameArrayList, charBinMap)
        }

        fun generateImage(image : BufferedImage, picturePath : String) : String  {
            val picturePathFile = File(picturePath)

            ImageIO.write(image, "png", picturePathFile)
            return picturePathFile.absolutePath
        }

        fun completeGroupsForPixelEmbedding(input: Int) : List<String> {
            val reversedBinList = Integer.toBinaryString(input).reversed().chunked(4) { it.padEnd(4, '0').toString() }
                .toMutableList()

            if (reversedBinList.size % bufferGrouping != 0) {
                val listLimit = ((reversedBinList.size / bufferGrouping) + 1) * bufferGrouping
                for (i in reversedBinList.size until listLimit) {
                    reversedBinList.add(padding)
                }
            }
            reversedBinList.reverse()

            val binList = mutableListOf<String>()
            reversedBinList.forEach {
                binList.add(it.reversed())
            }

            return binList
        }

        @Throws(OriginalPicTooSmallException::class)
        fun embedMessage(pictureToEncode : BufferedImage, pictureToHide: BufferedImage) : BufferedImage {
            val originalBufferedImg: BufferedImage = pictureToEncode
            val originalPicWidth: Int = originalBufferedImg.width
            val originalPicHeight: Int = originalBufferedImg.height
            val originalPicPixelArea: Int = originalPicHeight * originalPicWidth

            val picToHideBufferedImage: BufferedImage = pictureToHide
            val picToHideWidth: Int = picToHideBufferedImage.width
            val picToHideHeight: Int = picToHideBufferedImage.height
            val picToHidePixelArea: Int = picToHideWidth * picToHideHeight

            val picToHideWidthBinList = completeGroupsForPixelEmbedding(picToHideWidth)
            val picToHideHeightList = completeGroupsForPixelEmbedding(picToHideHeight)
            val picToHidePixelAreaBinList = picToHideWidthBinList + bufferDelimiterList + picToHideHeightList
            val totalPixelsForBuffer = picToHidePixelAreaBinList.size / bufferGrouping

            if (picToHidePixelArea > (originalPicPixelArea - totalPixelsForBuffer))
                throw OriginalPicTooSmallException()

            val newEncodedBufferedImage = BufferedImage(originalPicWidth, originalPicHeight, originalBufferedImg.type)

            /*val convertMessageToBinaryResult = convertMessageToBinary(name)
            val binConvertedNameArrayList = convertMessageToBinaryResult.first
            val binConvertedNameCharBinMap = convertMessageToBinaryResult.second*/

            val separator = System.lineSeparator()
            val file = File(".\\debugEn.txt")
            val outputStream = FileOutputStream(file)

            /*outputStream.write("$separator$separator".toByteArray())
            outputStream.write("Image in Binary...$separator".toByteArray())*/

            var pixelsLeftForBuffer = totalPixelsForBuffer
            var insertDelimiter = false
            var picToHideYIndex = 0
            var picToHideXIndex = -2
            var shouldEmbedPixel = true

            for (yIndex in (0 until originalPicHeight)) {
                for (xIndex in (0 until originalPicWidth)) {
                    val originalImgPixel = originalBufferedImg.getRGB(xIndex, yIndex)

                    if (shouldEmbedPixel) {
                        val originalImgPixelColor = Color(originalImgPixel)
                        val originalImgPixelRed = Integer.toBinaryString(originalImgPixelColor.red).padStart(8, '0').chunked(4)
                        val originalImgPixelGreen = Integer.toBinaryString(originalImgPixelColor.green).padStart(8, '0').chunked(4)
                        val originalImgPixelBlue = Integer.toBinaryString(originalImgPixelColor.blue).padStart(8, '0').chunked(4)
                        val originalImgPixelAlpha = Integer.toBinaryString(originalImgPixelColor.alpha).padStart(8, '0').chunked(4)
                        val originalImgRgbBinStr = Integer.toBinaryString(originalImgPixel).chunked(8).joinToString(" | ") { it.chunked(4).joinToString(" ") }

                        outputStream.write("Original Image Pixel (x:$xIndex, y:$yIndex): R - ${originalImgPixelRed.joinToString(" ")}, G - ${originalImgPixelGreen.joinToString(" ")}, B - ${originalImgPixelBlue.joinToString(" ")}, A - ${originalImgPixelAlpha.joinToString(" ")}".toByteArray())
                        outputStream.write(separator.toByteArray())
                        outputStream.write("Original Image Pixel (Full): $originalImgRgbBinStr".toByteArray())

                        outputStream.write(separator.toByteArray())

                        val picToHidePixelRed: List<String>
                        val picToHidePixelGreen: List<String>
                        val picToHidePixelBlue: List<String>
                        val picToHidePixelAlpha: List<String>
                        val picToHideRgbBinStr: String

                        if (pixelsLeftForBuffer > 0) {
                            val startingIndex = picToHidePixelAreaBinList.size - (pixelsLeftForBuffer * bufferGrouping)

                            picToHidePixelRed = listOf(picToHidePixelAreaBinList[startingIndex], padding)
                            picToHidePixelGreen = listOf(picToHidePixelAreaBinList[startingIndex + 1], padding)
                            picToHidePixelBlue = listOf(picToHidePixelAreaBinList[startingIndex + 2], padding)
                            picToHidePixelAlpha = originalImgPixelAlpha
                            picToHideRgbBinStr = picToHidePixelAlpha.joinToString(" ") + " | " + picToHidePixelRed.joinToString(" ") + " | " + picToHidePixelGreen.joinToString(" ") + " | " + picToHidePixelGreen.joinToString(" ")

                            outputStream.write("BUFFER $separator".toByteArray())

                            pixelsLeftForBuffer--

                            if (pixelsLeftForBuffer == 0)
                                insertDelimiter = true
                        }
                        else if (insertDelimiter) {
                            picToHidePixelRed = listOf(delimiterList[0], delimiterList[0])
                            picToHidePixelGreen = listOf(delimiterList[1], delimiterList[1])
                            picToHidePixelBlue = listOf(delimiterList[2], delimiterList[2])
                            picToHidePixelAlpha = originalImgPixelAlpha
                            picToHideRgbBinStr = picToHidePixelAlpha.joinToString(" ") + " | " +  picToHidePixelRed.joinToString(" ") + " | " + picToHidePixelGreen.joinToString(" ") + " | " + picToHidePixelGreen.joinToString(" ")

                            outputStream.write("DELIMITER $separator".toByteArray())
                            insertDelimiter = false
                        }
                        else {
                            val picToHidePixel = picToHideBufferedImage.getRGB(picToHideXIndex, picToHideYIndex)
                            val picToHidePixelColor = Color(picToHidePixel)

                            picToHidePixelRed = Integer.toBinaryString(picToHidePixelColor.red).padStart(8, '0').chunked(4)
                            picToHidePixelGreen = Integer.toBinaryString(picToHidePixelColor.green).padStart(8, '0').chunked(4)
                            picToHidePixelBlue = Integer.toBinaryString(picToHidePixelColor.blue).padStart(8, '0').chunked(4)
                            picToHidePixelAlpha = Integer.toBinaryString(picToHidePixelColor.alpha).padStart(8, '0').chunked(4)
                            picToHideRgbBinStr = Integer.toBinaryString(picToHidePixel).chunked(8).joinToString(" | ") { it.chunked(4).joinToString(" ") }
                        }

                        outputStream.write("Picture To Hide Image Pixel (x:$picToHideXIndex, y:$picToHideYIndex): R - ${picToHidePixelRed.joinToString(" ")}, G - ${picToHidePixelGreen.joinToString(" ")}, B - ${picToHidePixelBlue.joinToString(" ")}, A - ${picToHidePixelAlpha.joinToString(" ")}".toByteArray())
                        outputStream.write(separator.toByteArray())
                        outputStream.write("Picture To Hide Image Pixel (Full): $picToHideRgbBinStr".toByteArray())

                        outputStream.write(separator.toByteArray())

                        val newImgPixelRed = listOf(originalImgPixelRed[0], picToHidePixelRed[0]).joinToString("")
                        val newImgPixelGreen = listOf(originalImgPixelGreen[0], picToHidePixelGreen[0]).joinToString("")
                        val newImgPixelBlue = listOf(originalImgPixelBlue[0], picToHidePixelBlue[0]).joinToString("")
                        val newImgPixelAlpha = listOf(originalImgPixelAlpha[0], picToHidePixelAlpha[0]).joinToString("")
                        val newImgRgbBinStr = newImgPixelAlpha + newImgPixelRed + newImgPixelGreen + newImgPixelBlue

                        outputStream.write("New Image Pixel (x:$xIndex, y:$yIndex): R - ${newImgPixelRed.chunked(4).joinToString(" ")}, G - ${newImgPixelGreen.chunked(4).joinToString(" ")}, B - ${newImgPixelBlue.chunked(4).joinToString(" ")}, A - ${newImgPixelAlpha.chunked(4).joinToString(" ")}".toByteArray())
                        outputStream.write(separator.toByteArray())
                        outputStream.write("New Image Pixel (Full): ${newImgRgbBinStr.chunked(8).joinToString(" | ") { it.chunked(4).joinToString(" ") }}".toByteArray())


                        outputStream.write(separator.toByteArray())
                        outputStream.write(separator.toByteArray())

                        newEncodedBufferedImage.setRGB(xIndex, yIndex, BigInteger(newImgRgbBinStr, 2).toInt())

                        /*if (yIndex - 1  > picToHideHeight || xIndex - 1  > picToHideWidth)
                            insertDelimiter = true*/
                    }
                    else {
                        newEncodedBufferedImage.setRGB(xIndex, yIndex, originalImgPixel)
                    }

                    if (picToHideYIndex + 1 == picToHideHeight && picToHideXIndex + 1 == picToHideWidth) {
                        insertDelimiter = true
                        picToHideYIndex = -100
                    }
                    else if (picToHideYIndex < 0)
                        shouldEmbedPixel = false

                    if (picToHideXIndex + 1 == picToHideWidth) {
                        picToHideXIndex = 0
                        picToHideYIndex++
                    }
                    else if (pixelsLeftForBuffer == 0)
                        picToHideXIndex++
                }
            }

            outputStream.flush()
            outputStream.close()

            return newEncodedBufferedImage
        }

        fun retrieveEncodedMessageFromImage(encodedPic: BufferedImage) : BufferedImage {
            var retrievedImgHeight = 100
            var retrievedImgWidth = 100

            val encodedBufferedImg = encodedPic
            var retrievedBufferedImg = BufferedImage(retrievedImgWidth, retrievedImgHeight, encodedBufferedImg.type)

            var bufferRetrieved = false
            val retrievedBuffer = StringBuilder("")
            var retrievedImgYIndex = 0
            var retrievedImgXIndex = 0

            val separator = System.lineSeparator()
            val file = File(".\\debugRet.txt")
            val outputStream = FileOutputStream(file)

            outer@ for (yIndex in (0 until encodedBufferedImg.height)) {
                for (xIndex in (0 until encodedBufferedImg.width)) {
                    val encodedImgPixel = encodedBufferedImg.getRGB(xIndex, yIndex)
                    val encodedImgPixelColor = Color(encodedImgPixel)
                    val encodedImgPixelRed = Integer.toBinaryString(encodedImgPixelColor.red).padStart(8, '0').chunked(4)
                    val encodedImgPixelGreen = Integer.toBinaryString(encodedImgPixelColor.green).padStart(8, '0').chunked(4)
                    val encodedImgPixelBlue = Integer.toBinaryString(encodedImgPixelColor.blue).padStart(8, '0').chunked(4)
                    val encodedImgPixelAlpha = Integer.toBinaryString(encodedImgPixelColor.alpha).padStart(8, '0').chunked(4)
                    val encodedImgRgbBinStr = Integer.toBinaryString(encodedImgPixel).chunked(8).joinToString(" | ") { it.chunked(4).joinToString(" ") }

                    outputStream.write("Encoded Image Pixel (x:$xIndex, y:$yIndex): R - ${encodedImgPixelRed.joinToString(" ")}, G - ${encodedImgPixelGreen.joinToString(" ")}, B - ${encodedImgPixelBlue.joinToString(" ")}, A - ${encodedImgPixelAlpha.joinToString(" ")}".toByteArray())
                    outputStream.write(separator.toByteArray())
                    outputStream.write("Encoded Image Pixel (Full): $encodedImgRgbBinStr".toByteArray())
                    outputStream.write(separator.toByteArray())

                    val retrievedBin = encodedImgPixelRed[1] + encodedImgPixelGreen[1] + encodedImgPixelBlue[1]

                    if (!bufferRetrieved) {
                        if (retrievedBin == delimiterList.joinToString("")) {
                            bufferRetrieved = true
                            retrievedImgHeight = BigInteger(retrievedBuffer.toString(), 2).toInt()

                            retrievedBufferedImg = BufferedImage(retrievedImgWidth, retrievedImgHeight, encodedBufferedImg.type)
                            outputStream.write("Width: $retrievedImgWidth, Height: $retrievedImgHeight $separator$separator".toByteArray())
                        }
                        else {
                            if (retrievedBin == bufferDelimiterList.joinToString("")) {
                                retrievedImgWidth = BigInteger(retrievedBuffer.toString(), 2).toInt()
                                retrievedBuffer.clear()
                            }
                            else
                                retrievedBuffer.append(retrievedBin)
                        }
                    }
                    else {
                        if (retrievedBin == delimiterList.joinToString("")) {
                            break@outer
                        }
                        else {
                            val retrievedImgPixelRed = listOf(encodedImgPixelRed[1], padding).joinToString("")
                            val retrievedImgPixelGreen = listOf(encodedImgPixelGreen[1], padding).joinToString("")
                            val retrievedImgPixelBlue = listOf(encodedImgPixelBlue[1], padding).joinToString("")
                            val retrievedImgPixelAlpha = listOf(encodedImgPixelAlpha[1], "1111").joinToString("")
                            val retrievedImgRgbBinStr = retrievedImgPixelAlpha + retrievedImgPixelRed + retrievedImgPixelGreen + retrievedImgPixelBlue

                            outputStream.write("Retrieved Image Pixel (x:$retrievedImgXIndex, y:$retrievedImgYIndex): R - ${retrievedImgPixelRed.chunked(4).joinToString(" ")}, G - ${retrievedImgPixelGreen.chunked(4).joinToString(" ")}, B - ${retrievedImgPixelBlue.chunked(4).joinToString(" ")}, A - ${retrievedImgPixelAlpha.chunked(4).joinToString(" ")}".toByteArray())
                            outputStream.write(separator.toByteArray())
                            outputStream.write("Retrieved Image Pixel (Full): ${retrievedImgRgbBinStr.chunked(8).joinToString(" | ") { it.chunked(4).joinToString(" ") }}".toByteArray())


                            outputStream.write(separator.toByteArray())
                            outputStream.write(separator.toByteArray())

                            retrievedBufferedImg.setRGB(retrievedImgXIndex, retrievedImgYIndex, BigInteger(retrievedImgRgbBinStr, 2).toInt())
                        }

                        if (retrievedImgXIndex + 1 == retrievedImgWidth) {
                            retrievedImgXIndex = 0
                            retrievedImgYIndex++
                        }
                        else
                            retrievedImgXIndex++
                    }
                }
            }

            outputStream.flush()
            outputStream.close()

            return retrievedBufferedImg
        }

        class OriginalPicTooSmallException : Exception() {
            val errMsg = "Original Picture is too small!"
        }
    }
}