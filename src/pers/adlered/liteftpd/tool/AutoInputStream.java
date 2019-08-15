package pers.adlered.liteftpd.tool;

/**
 * AutoInputStream can analyze Chinese encoding from InputStream, and print it out correctly.
 */

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AutoInputStream {
    private InputStream inputStream;
    private int cacheSize;

    public AutoInputStream(InputStream inputStream, int cacheSize) {
        this.inputStream = inputStream;
        this.cacheSize = cacheSize;
    }

    public String readLineAuto() throws IOException {
        byte[] storage = new byte[cacheSize];
        int c2Cursor = 0;
        while (true) {
            int available = 0;
            while (available == 0) {
                available = inputStream.available();
            }
            byte[] cache = new byte[available];
            inputStream.read(cache);
            int cursor = 0;
            int relativeLength = c2Cursor + cache.length;
            if (relativeLength <= cacheSize) {
                for (int i = c2Cursor; i < relativeLength; i++) {
                    storage[i] = cache[cursor];
                    ++cursor;
                    ++c2Cursor;
                }
                //Check if space exists, break it.
                if (new String(storage, "UTF-8").indexOf("\n") != -1) {
                    break;
                }
            } else {
                break;
            }
        }
        //Clean wasted space
        int storageCursor = 0;
        int firstEmptyMark = -1;
        boolean marked = false;
        for (byte i : storage) {
            if (i == 0) {
                if (!marked) {
                    marked = true;
                    firstEmptyMark = storageCursor;
                }
            }
            ++storageCursor;
        }
        if (firstEmptyMark == -1) firstEmptyMark = storage.length;
        System.out.println();
        //Init a fit size bytes.
        byte[] cleaned = new byte[firstEmptyMark];
        for (int i = 0; i < firstEmptyMark; i++) {
            cleaned[i] = storage[i];
        }
        String UTF8 = new String(cleaned, "UTF-8");
        String GB2312 = new String(cleaned, "GB2312");
        System.out.println("UTF: " + UTF8);
        System.out.println("GB: " + GB2312);
        String charset = CharsetSelector.getCharset(cleaned);
        System.out.println("Charset detect: " + charset);
        String bestMatch = new String(cleaned, charset);
        System.out.println("Best match: " + bestMatch);
        return bestMatch;
    }

    public String readLineInUTF8() throws IOException {
        return "";
    }

    public String readLineInGB2312() throws IOException {
        return "";
    }
}