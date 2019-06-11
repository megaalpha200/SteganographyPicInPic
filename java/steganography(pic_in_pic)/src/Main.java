import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        do {
            try {
                System.out.println("Hello! Welcome to Image Encoder (Data in Picture)!");
                System.out.println("Created by: Jose A. Alvarado");
                System.out.println("Copyright J.A.A. Productions 2019");
                System.out.println();
                System.out.println("Please select an option...");
                System.out.println("1. Encode");
                System.out.println("2. Decode");
                System.out.println("3. Quit");
                System.out.println();
                System.out.print("Choice: ");

                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                final String userChoice = bufferedReader.readLine();

                switch (Integer.parseInt(userChoice)) {
                    case 1:
                        System.out.println();
                        encode();
                        break;
                    case 2:
                        System.out.println();
                        decode();
                        break;
                    case 3:
                        System.out.println("Goodbye!");
                        return;

                    default:
                        throw new WrongMenuChoiceException();
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Please enter a number from 1 to 3!");
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println(e.toString());
            }

            System.out.println();
        } while (true);
    }

    private static void encode() throws IOException, SteganographyPic.OriginalPicTooSmallException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter the location of the image you wish to embed: ");
        final String embeddedImagPath = bufferedReader.readLine();
        System.out.println();
        System.out.print("Enter the location of the image to be encoded: ");
        final String originalPath = bufferedReader.readLine();
        System.out.println();
        System.out.print("Enter the location where you wish to save the new encoded image: ");
        final String newPath = bufferedReader.readLine();
        System.out.println();
        System.out.println("Encoding Image...");

        final String encodedImgSaveLoc = SteganographyPic.generateEncodedImage(SteganographyPic.embedMessage(ImageIO.read(new File(originalPath)), ImageIO.read(new File(embeddedImagPath))), newPath);
        System.out.println("Encoded Image Saved At: " + encodedImgSaveLoc);
        System.out.println("Image encoded successfully!");
    }

    private static void decode() throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter the location of the encoded image: ");
        final String picPath = bufferedReader.readLine();
        System.out.println();
        System.out.print("Enter the location where you wish to save the new retrieved image: ");
        final String retrievedImagePath = bufferedReader.readLine();
        System.out.println();
        System.out.println("Retrieving Image...");

        final String decodedImgSaveLoc = SteganographyPic.generateEncodedImage(SteganographyPic.retrieveEncodedImageFromImage(ImageIO.read(new File(picPath))), retrievedImagePath);
        System.out.println("Retrieved Image Saved At: " + decodedImgSaveLoc);
        System.out.println("Image decoded successfully!");
    }

    private static class WrongMenuChoiceException extends Exception {
        public String getMessage() {
            return "Invalid Choice!";
        }

        public String toString() {
            return this.getClass().getCanonicalName() + ": Invalid Choice!";
        }
    }
}