package stringmanip;

import java.util.ArrayList;
import java.util.List;

public class StringManipulations {

    public static List<String> chunkString(String s, int size) {
        final int listSize = (s.length() % size != 0) ? (s.length() / size) + 1 : s.length() / size;
        final ArrayList<String> generatedList = new ArrayList<>(0);

        for (int i = 0; i < listSize; i++) {
            String subStr;
            if ((size * (i+1)) > s.length()) {
                subStr = s.substring((size * i));
            }
            else {
                subStr = s.substring((size * i), (size * (i+1)));
            }

            generatedList.add(subStr);
        }

        return generatedList;
    }

    public static String reverseString(String s) {
        final StringBuilder revStringBuilder = new StringBuilder();
        final char[] inputCharArray = s.toCharArray();

        for (int i = s.length() - 1; i >= 0; i--) {
            revStringBuilder.append(inputCharArray[i]);
        }

        return revStringBuilder.toString();
    }

    public static String padLeftWithZeros(String s, int amount) {
        return String.format("%" + amount + "s", s).replace(" ", "0");
    }

    public static String padRightWithZeros(String s, int amount) {
        return String.format("%" + (amount * -1) + "s", s).replace(" ", "0");
    }
}
