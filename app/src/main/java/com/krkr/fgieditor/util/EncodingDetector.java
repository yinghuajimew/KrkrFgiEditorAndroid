package com.krkr.fgieditor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EncodingDetector {

    private static final Charset[] CHARSETS = {
            Charset.forName("Shift_JIS"),  // Japanese (932)
            StandardCharsets.UTF_8,
            StandardCharsets.UTF_16
    };

    /**
     * Detect the best encoding for a text file
     * Based on counting characters outside expected ranges
     * @param file File to detect encoding for
     * @return Detected charset
     */
    public static Charset detectEncoding(File file) {
        int minErrorCount = Integer.MAX_VALUE;
        Charset bestCharset = StandardCharsets.UTF_8;

        for (Charset charset : CHARSETS) {
            try {
                int errorCount = countErrorChars(file, charset);
                if (errorCount < minErrorCount) {
                    minErrorCount = errorCount;
                    bestCharset = charset;
                }
            } catch (IOException e) {
                // Skip this encoding
                continue;
            }
        }

        return bestCharset;
    }

    /**
     * Count characters outside expected Japanese/English ranges
     * @param file File to analyze
     * @param charset Charset to use for reading
     * @return Count of error characters
     */
    private static int countErrorChars(File file, Charset charset) throws IOException {
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), charset))) {
            int ch;
            while ((ch = reader.read()) != -1) {
                // Check if character is outside expected ranges
                if (ch < 0x0009 ||
                        (ch > 0x000a && ch < 0x000d) ||
                        (ch > 0x000d && ch < 0x0020) ||
                        (ch > 0x007e && ch < 0x3000) ||
                        (ch > 0x301f && ch < 0x3041) ||
                        (ch > 0x30ff && ch < 0x4e00) ||
                        (ch > 0x9fff && ch < 0xff01) ||
                        ch > 0xff9f) {
                    errorCount++;
                }
            }
        }

        return errorCount;
    }

    /**
     * Get encoding name for display
     * @param charset Charset
     * @return Display name
     */
    public static String getEncodingName(Charset charset) {
        if (charset.equals(Charset.forName("Shift_JIS"))) {
            return "Shift-JIS (932)";
        } else if (charset.equals(StandardCharsets.UTF_8)) {
            return "UTF-8";
        } else if (charset.equals(StandardCharsets.UTF_16)) {
            return "Unicode (UTF-16)";
        }
        return charset.displayName();
    }
}
