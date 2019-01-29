/*
 * Copyright Cypress Semiconductor Corporation, 2014-2018 All rights reserved.
 *
 * This software, associated documentation and materials ("Software") is
 * owned by Cypress Semiconductor Corporation ("Cypress") and is
 * protected by and subject to worldwide patent protection (UnitedStates and foreign), United States copyright laws and international
 * treaty provisions. Therefore, unless otherwise specified in a separate license agreement between you and Cypress, this Software
 * must be treated like any other copyrighted material. Reproduction,
 * modification, translation, compilation, or representation of this
 * Software in any other form (e.g., paper, magnetic, optical, silicon)
 * is prohibited without Cypress's express written permission.
 *
 * Disclaimer: THIS SOFTWARE IS PROVIDED AS-IS, WITH NO WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * NONINFRINGEMENT, IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE. Cypress reserves the right to make changes
 * to the Software without notice. Cypress does not assume any liability
 * arising out of the application or use of Software or any product or
 * circuit described in the Software. Cypress does not authorize its
 * products for use as critical components in any products where a
 * malfunction or failure may reasonably be expected to result in
 * significant injury or death ("High Risk Product"). By including
 * Cypress's product in a High Risk Product, the manufacturer of such
 * system or application assumes all risk of such use and in doing so
 * indemnifies Cypress against all liability.
 *
 * Use of this Software may be limited by and subject to the applicable
 * Cypress software license agreement.
 *
 *
 */

package com.cypress.cysmart.CommonUtils;

public class ConvertUtils {

    public static int byteToIntUnsigned(byte b) {
        /**
         * AND with 0xFF to prevent returning negative value
         */
        return b & 0xFF;
    }

    public static long intToLongUnsigned(int i) {
        return i & 0xFFFFFFFFl;
    }

    public static long byteArrayToLongLittleEndian(byte[] bytes) {
        return intToLongUnsigned(byteArrayToIntLittleEndian(bytes));
    }

    public static int byteArrayToIntLittleEndian(byte[] bytes) {
        return byteArrayToIntLittleEndian(bytes, 0, bytes.length);
    }

    public static int byteArrayToIntLittleEndian(byte[] bytes, int offset, int length) {
        int value = 0;
        if (offset >= 0 && length > 0 && offset + length <= bytes.length) {
            for (int i = offset + length - 1; i >= offset; i--) {
                value <<= 8;
                value += (byteToIntUnsigned(bytes[i]));
            }
        }
        return value;
    }

    public static byte[] hexStringToByteArrayLittleEndian(String string, int offset, int length) {
        return hexStringToByteArray(string, offset, length, length, true);
    }

    public static byte[] hexStringToByteArrayLittleEndian(String string, int offset, int length, int expectedLength) {
        return hexStringToByteArray(string, offset, length, expectedLength, true);
    }

    public static byte[] hexStringToByteArrayBigEndian(String string, int offset, int length) {
        return hexStringToByteArray(string, offset, length, length, false);
    }

    public static byte[] hexStringToByteArrayBigEndian(String string, int offset, int length, int expectedLength) {
        return hexStringToByteArray(string, offset, length, expectedLength, false);
    }

    private static byte[] hexStringToByteArray(String string, int offset, int length, int expectedLength, boolean isLittleEndian) {
        boolean isOddNumChars = length % 2 == 1;
        int numBytes = length / 2 + length % 2;
        int expectedNumBytes = expectedLength / 2 + expectedLength % 2;

        int maxNumBytes = Math.max(numBytes, expectedNumBytes);
        byte[] bytes = new byte[maxNumBytes];

        for (int i = 0, n = numBytes; i < n; i++) {
            int idx;
            if (isLittleEndian) {
                idx = i;
            } else {
                idx = n - 1 - i;
            }

            boolean isHalfByte;
            if (isLittleEndian) {
                isHalfByte = i == n - 1 & isOddNumChars;
            } else {
                isHalfByte = i == 0 & isOddNumChars;
            }

            byte b = hexStringToByte(string, offset, isHalfByte);

            if (isLittleEndian && isHalfByte) {
                b <<= 4;
            }
            bytes[idx] = b;

            if (isHalfByte) {
                offset++;
            } else {
                offset += 2;
            }
        }

        if (expectedNumBytes < numBytes) {
            byte[] tmp = new byte[expectedNumBytes];
            System.arraycopy(bytes, 0, tmp, 0, tmp.length);
            bytes = tmp;
        }

        return bytes;
    }

    public static byte hexStringToByte(String string, int offset, boolean isHalfByte) {
        String s = string.substring(offset, offset + (isHalfByte ? 1 : 2));
        byte value = 0;
        for (int i = 0; i < s.length(); i++) {
            value <<= 4;
            value += charToByte(s.charAt(i));
        }
        return value;
    }

    public static byte charToByte(char value) {
        if ('0' <= value && value <= '9')
            return (byte) (value - '0');
        if ('a' <= value && value <= 'f')
            return (byte) (10 + value - 'a');
        if ('A' <= value && value <= 'F')
            return (byte) (10 + value - 'A');
        return 0;
    }

    public static byte[] byteArraySubset(byte[] bytes, int offset, int length) {
        byte[] subset;
        if (offset >= 0 && length >= 0 && offset + length <= bytes.length) {
            subset = new byte[length];
            System.arraycopy(bytes, offset, subset, 0, length);
        } else {
            subset = new byte[0];
        }
        return subset;
    }

    public static byte[] intToByteArray(int value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) (value >> (i * 8));
        }
        return bytes;
    }

    /**
     * Byte swap a single int value.
     *
     * @param value Value to byte swap.
     * @return Byte swapped representation.
     */
    public static int swapShort(int value) {
        int b1 = (value >> 0) & 0xff;
        int b2 = (value >> 8) & 0xff;
        return 0xFFFF & (b1 << 8 | b2 << 0);
    }
}
