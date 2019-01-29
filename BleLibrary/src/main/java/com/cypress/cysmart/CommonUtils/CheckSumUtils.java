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

public class CheckSumUtils {
    /* 16-bit CRC checksum using the CCITT implementation */
    public final static int CRC_CHECKSUM = 0x01;

    /**
     * Method to calculate 2's complement
     */
    public static int calculateCheckSum2(int checkSumType, byte[] data, int dataLength) {
        int checkSum = 0;
        if (checkSumType == CRC_CHECKSUM) {
            return CRC16.crc16(data, dataLength);
        } else {
            while (dataLength-- > 0) {
                checkSum += ConvertUtils.byteToIntUnsigned(data[dataLength]);
            }
        }
        return 1 + (~checkSum);
    }

    public static long crc32(byte[] buf, long size) {
        return CRC32.crc32(buf, size);
    }

    /**
     * MMethod to calculate 2's complement in Verify row command
     *
     * @param datalen
     * @param data
     * @return
     */
    public static int calculateChecksumVerifyRow(int datalen, byte[] data) {
        int checkSum = 0;
        while (datalen-- > 0) {
            /**
             * AND each value with 0xFF to remove the negative value for summation
             */
            checkSum += (data[datalen] & 0xFF);
        }
        return checkSum;
    }

    private static class CRC16 {
        /**
         * CRC checkSum Method
         *
         * @param data
         * @return
         */
        private static int crc16(byte[] data, int dataLength) {
            int crc = 0xffff;
            int i, tmp;
            if (dataLength == 0) {
                return (~crc);
            }

            for (int h = 0; h < dataLength; ++h) {
                for (i = 0, tmp = (0x00ff & data[h]); i < 8; i++, tmp >>= 1) {
                    if (((crc & 0x0001) ^ (tmp & 0x0001)) > 0) {
                        crc = (crc >> 1) ^ 0x8408;
                    } else {
                        crc >>= 1;
                    }
                }
            }

            crc = ~crc;
            tmp = crc;

            crc = (crc << 8) | (tmp >> 8 & 0xFF);
            crc = 0xffff & crc;

            return crc;
        }
    }

    private static class CRC32 {

        private static final long g0 = 0x82F63B78l;
        private static final long g1 = (g0 >> 1) & 0x7FFFFFFFl;
        private static final long g2 = (g0 >> 2) & 0x3FFFFFFFl;
        private static final long g3 = (g0 >> 3) & 0x1FFFFFFFl;

        private static final long table[] = {
                0, g3, g2, (g2 ^ g3),
                g1, (g1 ^ g3), (g1 ^ g2), (g1 ^ g2 ^ g3),
                g0, (g0 ^ g3), (g0 ^ g2), (g0 ^ g2 ^ g3),
                (g0 ^ g1), (g0 ^ g1 ^ g3), (g0 ^ g1 ^ g2), (g0 ^ g1 ^ g2 ^ g3),
        };

        private static long crc32(byte[] buf, long size) {
            int pos = 0;
            long crc = 0xFFFFFFFFl;
            while (size != 0) {
                int i;
                --size;
                crc = crc ^ (buf[pos] & 0xFFl);
                ++pos;
                for (i = 1; i >= 0; i--) {
                    crc = (crc >> 4) ^ table[(int) (crc & 0xFl)];
                }
            }
            return (~crc) & 0xFFFFFFFFl;
        }
    }
}
