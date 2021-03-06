/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.StringWriterIOException;
import groovy.lang.Writable;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * This class defines all the encoding/decoding groovy methods which enhance
 * the normal JDK classes when inside the Groovy environment.
 * Static methods are used with the first parameter the destination class.
 */
public class EncodingGroovyMethods {

    private static final char[] T_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();

    private static final String CHUNK_SEPARATOR = "\r\n";

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data Byte array to be encoded
     * @param chunked whether or not the Base64 encoded data should be MIME chunked
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.5.1
     */
    public static Writable encodeBase64(Byte[] data, final boolean chunked) {
        return encodeBase64(DefaultTypeTransformation.convertToByteArray(data), chunked);
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data Byte array to be encoded
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.0
     */
    public static Writable encodeBase64(Byte[] data) {
        return encodeBase64(DefaultTypeTransformation.convertToByteArray(data), false);
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data byte array to be encoded
     * @param chunked whether or not the Base64 encoded data should be MIME chunked
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.5.7
     */
    public static Writable encodeBase64(final byte[] data, final boolean chunked) {
        return new Writable() {
            public Writer writeTo(final Writer writer) throws IOException {
                int charCount = 0;
                final int dLimit = (data.length / 3) * 3;

                for (int dIndex = 0; dIndex != dLimit; dIndex += 3) {
                    int d = ((data[dIndex] & 0XFF) << 16) | ((data[dIndex + 1] & 0XFF) << 8) | (data[dIndex + 2] & 0XFF);

                    writer.write(T_TABLE[d >> 18]);
                    writer.write(T_TABLE[(d >> 12) & 0X3F]);
                    writer.write(T_TABLE[(d >> 6) & 0X3F]);
                    writer.write(T_TABLE[d & 0X3F]);

                    if (chunked && ++charCount == 19) {
                        writer.write(CHUNK_SEPARATOR);
                        charCount = 0;
                    }
                }

                if (dLimit != data.length) {
                    int d = (data[dLimit] & 0XFF) << 16;

                    if (dLimit + 1 != data.length) {
                        d |= (data[dLimit + 1] & 0XFF) << 8;
                    }

                    writer.write(T_TABLE[d >> 18]);
                    writer.write(T_TABLE[(d >> 12) & 0X3F]);
                    writer.write((dLimit + 1 < data.length) ? T_TABLE[(d >> 6) & 0X3F] : '=');
                    writer.write('=');
                    if (chunked && charCount != 0) {
                        writer.write(CHUNK_SEPARATOR);
                    }
                }

                return writer;
            }

            public String toString() {
                StringWriter buffer = new StringWriter();

                try {
                    writeTo(buffer);
                } catch (IOException e) {
                    throw new StringWriterIOException(e);
                }

                return buffer.toString();
            }
        };
    }

    /**
     * Produce a Writable object which writes the Base64 encoding of the byte array.
     * Calling toString() on the result returns the encoding as a String. For more
     * information on Base64 encoding and chunking see <code>RFC 4648</code>.
     *
     * @param data byte array to be encoded
     * @return object which will write the Base64 encoding of the byte array
     * @since 1.0
     */
    public static Writable encodeBase64(final byte[] data) {
        return encodeBase64(data, false);
    }

    private static final byte[] TRANSLATE_TABLE = (
            //
            "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //                    \t    \n                \r
                    + "\u0042\u0042\u0041\u0041\u0042\u0042\u0041\u0042"
                    //
                    + "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //
                    + "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //        sp    !     "     #     $     %     &     '
                    + "\u0041\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //         (    )     *     +     ,     -     .     /
                    + "\u0042\u0042\u0042\u003E\u0042\u0042\u0042\u003F"
                    //         0    1     2     3     4     5     6     7
                    + "\u0034\u0035\u0036\u0037\u0038\u0039\u003A\u003B"
                    //         8    9     :     ;     <     =     >     ?
                    + "\u003C\u003D\u0042\u0042\u0042\u0040\u0042\u0042"
                    //         @    A     B     C     D     E     F     G
                    + "\u0042\u0000\u0001\u0002\u0003\u0004\u0005\u0006"
                    //         H    I   J K   L     M   N   O
                    + "\u0007\u0008\t\n\u000B\u000C\r\u000E"
                    //         P    Q     R     S     T     U     V    W
                    + "\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016"
                    //         X    Y     Z     [     \     ]     ^    _
                    + "\u0017\u0018\u0019\u0042\u0042\u0042\u0042\u0042"
                    //         '    a     b     c     d     e     f     g
                    + "\u0042\u001A\u001B\u001C\u001D\u001E\u001F\u0020"
                    //        h   i   j     k     l     m     n     o    p
                    + "\u0021\"\u0023\u0024\u0025\u0026\u0027\u0028"
                    //        p     q     r     s     t     u     v     w
                    + "\u0029\u002A\u002B\u002C\u002D\u002E\u002F\u0030"
                    //        x     y     z
                    + "\u0031\u0032\u0033").getBytes();

    /**
     * Decode the String from Base64 into a byte array.
     *
     * @param value the string to be decoded
     * @return the decoded bytes as an array
     * @since 1.0
     */
    public static byte[] decodeBase64(String value) {
        int byteShift = 4;
        int tmp = 0;
        boolean done = false;
        final StringBuilder buffer = new StringBuilder();

        for (int i = 0; i != value.length(); i++) {
            final char c = value.charAt(i);
            final int sixBit = (c < 123) ? TRANSLATE_TABLE[c] : 66;

            if (sixBit < 64) {
                if (done)
                    throw new RuntimeException("= character not at end of base64 value"); // TODO: change this exception type

                tmp = (tmp << 6) | sixBit;

                if (byteShift-- != 4) {
                    buffer.append((char) ((tmp >> (byteShift * 2)) & 0XFF));
                }

            } else if (sixBit == 64) {

                byteShift--;
                done = true;

            } else if (sixBit == 66) {
                // RFC 2045 says that I'm allowed to take the presence of
                // these characters as evedence of data corruption
                // So I will
                throw new RuntimeException("bad character in base64 value"); // TODO: change this exception type
            }

            if (byteShift == 0) byteShift = 4;
        }

        try {
            return buffer.toString().getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Base 64 decode produced byte values > 255"); // TODO: change this exception type
        }
    }


}