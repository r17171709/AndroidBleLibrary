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

package com.cypress.cysmart.DataModelClasses;

//NOTE: all byte arrays are in Little Endian format
public abstract class OTAFlashRowModel_v1 {

    public static class Header extends OTAFlashRowModel_v1 {

        public byte mFileVersion;
        public byte[] mSiliconId;
        public byte mSiliconRev;
        public byte mCheckSumType;
        public byte mAppId;
        public byte[] mProductId;

        public Header(byte fileVersion, byte[] siliconId, byte siliconRev, byte checkSumType, byte appId, byte[] productId) {
            this.mFileVersion = fileVersion;
            this.mSiliconId = siliconId;
            this.mSiliconRev = siliconRev;
            this.mCheckSumType = checkSumType;
            this.mAppId = appId;
            this.mProductId = productId;
        }
    }

    public static class Data extends OTAFlashRowModel_v1 {

        public static final String DISCRIMINATOR = ":";
        public byte[] mAddress;
        public byte[] mData;

        public Data(byte[] address, byte[] data) {
            this.mAddress = address;
            this.mData = data;
        }
    }

    /**
     * Encryption Initialization Vector
     */
    public static class EIV extends OTAFlashRowModel_v1 {

        public static final String DISCRIMINATOR = "@EIV:";
        public byte[] mEiv;

        public EIV(byte[] eiv) {
            this.mEiv = eiv;
        }
    }

    public static class AppInfo extends OTAFlashRowModel_v1 {

        public static final String DISCRIMINATOR = "@APPINFO:0x";
        public static final String SEPARATOR = ",0x";
        public byte[] mAppStart;
        public byte[] mAppSize;

        public AppInfo(byte[] appStart, byte[] appSize) {
            this.mAppStart = appStart;
            this.mAppSize = appSize;
        }
    }

    private OTAFlashRowModel_v1() {

    }
}
