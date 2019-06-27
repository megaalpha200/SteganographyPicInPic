import stringmanip.StringManipulations;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SteganographyPic {
    private static final List<String> bufferDelimiterList = new ArrayList<String>() {
        {
            add("1010");
            add("1111");
            add("0101");
        }
    };
    private static final List<String> delimiterList = new ArrayList<String>() {
        {
            add("1111");
            add("1010");
            add("1111");
        }
    };
    private static final String padding = "0000";
    private static final int bufferGrouping = 3;
    private static final String separator = System.lineSeparator();

    public static void main(String[] args) {
        System.out.println();
    }

    public static String generateEncodedImage(BufferedImage image, String picturePath) throws IOException {
        final File picturePathFile = new File(picturePath);

        ImageIO.write(image, "png", picturePathFile);
        return picturePathFile.getAbsolutePath();
    }

    private static List<String> completeGroupsForPixelEmbedding(int input) {
        final List<String> reversedBinListNoPadding = StringManipulations.chunkString(StringManipulations.reverseString(Integer.toBinaryString(input)), 4);
        final ArrayList<String> reversedBinList = new ArrayList<>(0);

        for (String s : reversedBinListNoPadding) {
            reversedBinList.add(StringManipulations.padRightWithZeros(s, 4));
        }

        if (reversedBinList.size() % bufferGrouping != 0) {
            final int listLimit = ((reversedBinList.size() / bufferGrouping) + 1) * bufferGrouping;
            for (int i = reversedBinList.size(); i < listLimit; i++) {
                reversedBinList.add(padding);
            }
        }

        final ArrayList<String> binList = new ArrayList<>(0);
        for (int j = reversedBinList.size() - 1; j >= 0; j--) {
            binList.add(StringManipulations.reverseString(reversedBinList.get(j)));
        }

        return binList;
    }

    public static BufferedImage embedMessage(BufferedImage pictureToEncode, BufferedImage pictureToHide) throws OriginalPicTooSmallException, IOException {
        final BufferedImage originalBufferedImg = pictureToEncode;
        final int originalPicWidth = originalBufferedImg.getWidth();
        final int originalPicHeight = originalBufferedImg.getHeight();
        final int originalPicPixelArea = originalPicHeight * originalPicWidth;

        final BufferedImage picToHideBufferedImage = pictureToHide;
        final int picToHideWidth = picToHideBufferedImage.getWidth();
        final int picToHideHeight = picToHideBufferedImage.getHeight();
        final int picToHidePixelArea = picToHideHeight * picToHideWidth;

        final List<String> picToHideWidthBinList = completeGroupsForPixelEmbedding(picToHideWidth);
        final List<String> picToHideHeightBinList = completeGroupsForPixelEmbedding(picToHideHeight);
        final ArrayList<String> picToHidePixelAreaBinList = new ArrayList<String>() {
            {
                addAll(picToHideWidthBinList);
                addAll(bufferDelimiterList);
                addAll(picToHideHeightBinList);
            }
        };
        final int totalPixelsForBuffer = picToHidePixelAreaBinList.size() / bufferGrouping;

        if (picToHidePixelArea > (originalPicPixelArea - totalPixelsForBuffer)) {
            throw new OriginalPicTooSmallException();
        }

        final BufferedImage newEncodedBufferedImage = new BufferedImage(originalPicWidth, originalPicHeight, originalBufferedImg.getType());

        final File file = new File(".\\debugEn.txt");
        final FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(("Image in Binary..." + separator).getBytes());

        int pixelsLeftForBuffer = totalPixelsForBuffer;
        boolean insertDelimiter = false;
        int picToHideYIndex = 0;
        int picToHideXIndex = -2;
        boolean shouldEmbedPixel = true;

        for (int yIndex = 0; yIndex < originalPicHeight; yIndex++) {
            for (int xIndex = 0; xIndex < originalPicWidth; xIndex++) {
                final int originalImgPixel = originalBufferedImg.getRGB(xIndex, yIndex);

                if (shouldEmbedPixel) {
                    final Color originalImgPixelColor = new Color(originalImgPixel);
                    final List<String> originalImgPixelRed = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(originalImgPixelColor.getRed()), 8),4);
                    final List<String> originalImgPixelGreen = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(originalImgPixelColor.getGreen()), 8),4);
                    final List<String> originalImgPixelBlue = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(originalImgPixelColor.getBlue()), 8),4);
                    final List<String> originalImgPixelAlpha = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(originalImgPixelColor.getAlpha()), 8),4);
                    final String originalImgRgbBinStr = String.join(" | ", StringManipulations.chunkString(Integer.toBinaryString(originalImgPixel), 8));

                    outputStream.write(("Original Image Pixel (x:" + xIndex + ", y:" + yIndex + "): R - " + String.join(" ", originalImgPixelRed) + " G - " + String.join(" ", originalImgPixelGreen) + " B - " + String.join(" ", originalImgPixelBlue) + " A - " + String.join(" ", originalImgPixelAlpha)).getBytes());
                    outputStream.write(separator.getBytes());
                    outputStream.write(("Original Image Pixel (Full): " + originalImgRgbBinStr).getBytes());

                    outputStream.write(separator.getBytes());

                    final List<String> picToHidePixelRed;
                    final List<String> picToHidePixelGreen;
                    final List<String> picToHidePixelBlue;
                    final List<String> picToHidePixelAlpha;
                    final String picToHideRgbBinStr;

                    if (pixelsLeftForBuffer > 0) {
                        final int startIndex = picToHidePixelAreaBinList.size() - (pixelsLeftForBuffer * bufferGrouping);

                        picToHidePixelRed = new ArrayList<String>() {{ add(picToHidePixelAreaBinList.get(startIndex)); add(padding); }};
                        picToHidePixelGreen = new ArrayList<String>() {{ add(picToHidePixelAreaBinList.get(startIndex + 1)); add(padding); }};
                        picToHidePixelBlue = new ArrayList<String>() {{ add(picToHidePixelAreaBinList.get(startIndex + 2)); add(padding); }};
                        picToHidePixelAlpha = originalImgPixelAlpha;
                        picToHideRgbBinStr = String.join(" ", picToHidePixelAlpha) + " | " + String.join(" ", picToHidePixelRed) + " | " + String.join(" ", picToHidePixelGreen) + " | " + String.join(" ", picToHidePixelBlue);

                        outputStream.write(("BUFFER " + separator).getBytes());

                        pixelsLeftForBuffer--;

                        if (pixelsLeftForBuffer == 0) {
                            insertDelimiter = true;
                        }
                    }
                    else if (insertDelimiter) {
                        picToHidePixelRed = new ArrayList<String>() {{ add(delimiterList.get(0)); add(delimiterList.get(0)); }};
                        picToHidePixelGreen = new ArrayList<String>() {{ add(delimiterList.get(1)); add(delimiterList.get(1)); }};
                        picToHidePixelBlue = new ArrayList<String>() {{ add(delimiterList.get(2)); add(delimiterList.get(2)); }};
                        picToHidePixelAlpha = originalImgPixelAlpha;
                        picToHideRgbBinStr = String.join(" ", picToHidePixelAlpha) + " | " + String.join(" ", picToHidePixelRed) + " | " + String.join(" ", picToHidePixelGreen) + " | " + String.join(" ", picToHidePixelBlue);

                        outputStream.write(("DELIITER " + separator).getBytes());
                        insertDelimiter = false;
                    }
                    else {
                        final int picToHidePixel = picToHideBufferedImage.getRGB(picToHideXIndex, picToHideYIndex);
                        final Color picToHidePixelColor = new Color(picToHidePixel);

                        picToHidePixelRed = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(picToHidePixelColor.getRed()), 8), 4);
                        picToHidePixelGreen = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(picToHidePixelColor.getGreen()), 8), 4);;
                        picToHidePixelBlue = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(picToHidePixelColor.getBlue()), 8), 4);;
                        picToHidePixelAlpha = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(picToHidePixelColor.getAlpha()), 8), 4);;
                        picToHideRgbBinStr = String.join(" | ", StringManipulations.chunkString(Integer.toBinaryString(picToHidePixel), 8));
                    }

                    outputStream.write(("Picture To Hide Image Pixel (x:" + picToHideXIndex + ", y:" + picToHideYIndex + "): R - " + String.join(" ", picToHidePixelRed) + " G - " + String.join(" ", picToHidePixelGreen) + " B - " + String.join(" ", picToHidePixelBlue) + " A - " + String.join(" ", picToHidePixelAlpha)).getBytes());
                    outputStream.write(separator.getBytes());
                    outputStream.write(("Picture To Hide Image Pixel (Full): " + picToHideRgbBinStr).getBytes());

                    outputStream.write(separator.getBytes());

                    final String newImgPixelRed = String.join("", new ArrayList<String>() {{ add(originalImgPixelRed.get(0)); add(picToHidePixelRed.get(0)); }});
                    final String newImgPixelGreen = String.join("", new ArrayList<String>() {{ add(originalImgPixelGreen.get(0)); add(picToHidePixelGreen.get(0)); }});
                    final String newImgPixelBlue = String.join("", new ArrayList<String>() {{ add(originalImgPixelBlue.get(0)); add(picToHidePixelBlue.get(0)); }});
                    final String newImgPixelAlpha = String.join("", new ArrayList<String>() {{ add(originalImgPixelAlpha.get(0)); add(picToHidePixelAlpha.get(0)); }});
                    final String newImgRgbBinStr = newImgPixelAlpha + newImgPixelRed + newImgPixelGreen + newImgPixelBlue;

                    outputStream.write(("New Image Pixel (x:" + xIndex + ", y:" + yIndex + "): R - " + String.join(" ", StringManipulations.chunkString(newImgPixelRed, 4)) + " G - " + String.join(" ", StringManipulations.chunkString(newImgPixelGreen, 4)) + " B - " + String.join(" ", StringManipulations.chunkString(newImgPixelBlue, 4)) + " A - " + String.join(" ", StringManipulations.chunkString(newImgPixelAlpha, 4))).getBytes());
                    outputStream.write(separator.getBytes());
                    outputStream.write(("New Image Pixel (Full): " + String.join(" | ", StringManipulations.chunkString(newImgRgbBinStr, 8))).getBytes());

                    outputStream.write(separator.getBytes());
                    outputStream.write(separator.getBytes());

                    newEncodedBufferedImage.setRGB(xIndex, yIndex, (new BigInteger(newImgRgbBinStr, 2)).intValue());
                }
                else {
                    newEncodedBufferedImage.setRGB(xIndex, yIndex, originalImgPixel);
                }

                if (picToHideYIndex + 1 == picToHideHeight && picToHideXIndex + 1 == picToHideWidth) {
                    insertDelimiter = true;
                    picToHideYIndex = -100;
                }
                else if (picToHideYIndex < 0)
                    shouldEmbedPixel = false;

                if (picToHideXIndex + 1 == picToHideWidth) {
                    picToHideXIndex = 0;
                    picToHideYIndex++;
                }
                else if (pixelsLeftForBuffer == 0)
                    picToHideXIndex++;
            }
        }

        outputStream.flush();
        outputStream.close();

        return newEncodedBufferedImage;
    }

    public static BufferedImage retrieveEncodedImageFromImage(BufferedImage encodedPic) throws IOException {
        int retrievedImgHeight = 100;
        int retrievedImgWidth = 100;

        final BufferedImage encodedBufferedImg = encodedPic;
        BufferedImage retrievedBufferedImg = new BufferedImage(retrievedImgWidth, retrievedImgHeight, encodedBufferedImg.getType());

        boolean bufferRetrieved = false;
        StringBuilder retrievedBuffer = new StringBuilder();
        int retrievedImgYIndex = 0;
        int retrievedImgXIndex = 0;

        final File file = new File(".\\debugRet.txt");
        final FileOutputStream outputStream = new FileOutputStream(file);

        outer: for (int yIndex = 0; yIndex < encodedBufferedImg.getHeight(); yIndex++) {
            for (int xIndex = 0; xIndex < encodedBufferedImg.getWidth(); xIndex++) {

                final int encodedImgPixel = encodedBufferedImg.getRGB(xIndex, yIndex);
                final Color encodedImgPixelColor = new Color(encodedImgPixel);
                final List<String> encodedImgPixelRed = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(encodedImgPixelColor.getRed()), 8),4);
                final List<String> encodedImgPixelGreen = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(encodedImgPixelColor.getGreen()), 8),4);
                final List<String> encodedImgPixelBlue = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(encodedImgPixelColor.getBlue()), 8),4);
                final List<String> encodedImgPixelAlpha = StringManipulations.chunkString(StringManipulations.padLeftWithZeros(Integer.toBinaryString(encodedImgPixelColor.getAlpha()), 8),4);
                final String encodedImgRgbBinStr = String.join(" | ", StringManipulations.chunkString(Integer.toBinaryString(encodedImgPixel), 8));

                outputStream.write(("Encoded Image Pixel (x:" + xIndex + ", y:" + yIndex + "): R - " + String.join(" ", encodedImgPixelRed) + " G - " + String.join(" ", encodedImgPixelGreen) + " B - " + String.join(" ", encodedImgPixelBlue) + " A - " + String.join(" ", encodedImgPixelAlpha)).getBytes());
                outputStream.write(separator.getBytes());
                outputStream.write(("Encoded Image Pixel (Full): " + encodedImgRgbBinStr).getBytes());
                outputStream.write(separator.getBytes());

                final String retrievedBin = encodedImgPixelRed.get(1) + encodedImgPixelGreen.get(1) + encodedImgPixelBlue.get(1);

                if (!bufferRetrieved) {
                    if (retrievedBin.equals(String.join("", delimiterList))) {
                        bufferRetrieved = true;
                        retrievedImgHeight = new BigInteger(retrievedBuffer.toString(), 2).intValue();

                        outputStream.write(("Width: " + retrievedImgWidth + ", Height: " + retrievedImgHeight + separator + separator).getBytes());
                        retrievedBufferedImg = new BufferedImage(retrievedImgWidth, retrievedImgHeight, encodedBufferedImg.getType());
                    }
                    else if (retrievedBin.equals(String.join("", bufferDelimiterList))) {
                        retrievedImgWidth = new BigInteger(retrievedBuffer.toString(), 2).intValue();
                        retrievedBuffer = new StringBuilder();
                    }
                    else
                        retrievedBuffer.append(retrievedBin);
                }
                else {
                    if (retrievedBin.equals(String.join("", delimiterList)))
                        break outer;
                    else {
                        final String retrievedImgPixelRed = String.join("", new ArrayList<String>() {{ add(encodedImgPixelRed.get(1)); add(padding); }});
                        final String retrievedImgPixelGreen = String.join("", new ArrayList<String>() {{ add(encodedImgPixelGreen.get(1)); add(padding); }});
                        final String retrievedImgPixelBlue = String.join("", new ArrayList<String>() {{ add(encodedImgPixelBlue.get(1)); add(padding); }});
                        final String retrievedImgPixelAlpha = String.join("", new ArrayList<String>() {{ add(encodedImgPixelAlpha.get(1)); add(padding); }});
                        final String retrievedImgRgbBinStr = retrievedImgPixelAlpha + retrievedImgPixelRed + retrievedImgPixelGreen + retrievedImgPixelBlue;

                        outputStream.write(("Retrieved Image Pixel (x:" + retrievedImgXIndex + ", y:" + retrievedImgYIndex + "): R - " + String.join(" ", StringManipulations.chunkString(retrievedImgPixelRed, 4)) + " G - " + String.join(" ", StringManipulations.chunkString(retrievedImgPixelGreen, 4)) + " B - " + String.join(" ", StringManipulations.chunkString(retrievedImgPixelBlue, 4)) + " A - " + String.join(" ", StringManipulations.chunkString(retrievedImgPixelAlpha, 4))).getBytes());
                        outputStream.write(separator.getBytes());
                        outputStream.write(("Retrieved Image Pixel (Full): " + String.join(" | ", StringManipulations.chunkString(retrievedImgRgbBinStr, 8))).getBytes());

                        outputStream.write(separator.getBytes());
                        outputStream.write(separator.getBytes());

                        retrievedBufferedImg.setRGB(retrievedImgXIndex, retrievedImgYIndex, (new BigInteger(retrievedImgRgbBinStr, 2).intValue()));
                    }

                    if (retrievedImgXIndex + 1 == retrievedImgWidth) {
                        retrievedImgXIndex = 0;
                        retrievedImgYIndex++;
                    }
                    else
                        retrievedImgXIndex++;
                }
            }
        }

        outputStream.flush();
        outputStream.close();

        return retrievedBufferedImg;
    }

    public static class OriginalPicTooSmallException extends Exception {
        public String getMessage() {
            return "Original Picture is too small!";
        }

        public String toString() {
            return this.getClass().getCanonicalName() + ": " + this.getMessage();
        }
    }
}
