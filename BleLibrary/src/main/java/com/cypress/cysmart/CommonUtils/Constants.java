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

/**
 * Constants used in the project
 */
public class Constants {

    /**
     * Extras Constants
     */
    public static final String EXTRA_BYTE_VALUE = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_BYTE_VALUE";
    public static final String EXTRA_BYTE_UUID_VALUE = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_BYTE_UUID_VALUE";
    public static final String EXTRA_BYTE_INSTANCE_VALUE = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_BYTE_INSTANCE_VALUE";
    public static final String EXTRA_BYTE_SERVICE_UUID_VALUE = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_BYTE_SERVICE_UUID_VALUE";
    public static final String EXTRA_BYTE_SERVICE_INSTANCE_VALUE = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_BYTE_SERVICE_INSTANCE_VALUE";

    /**
     * Descriptor constants
     */
    public static final String EXTRA_SILICON_ID = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_SILICON_ID";
    public static final String EXTRA_SILICON_REV = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_SILICON_REV";
    public static final String EXTRA_APP_VALID = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_APP_VALID";
    public static final String EXTRA_APP_ACTIVE = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_APP_ACTIVE";
    public static final String EXTRA_START_ROW = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_START_ROW";
    public static final String EXTRA_END_ROW = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_END_ROW";
    public static final String EXTRA_SEND_DATA_ROW_STATUS = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_SEND_DATA_ROW_STATUS";
    public static final String EXTRA_PROGRAM_ROW_STATUS = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_PROGRAM_ROW_STATUS";
    public static final String EXTRA_VERIFY_ROW_STATUS = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_VERIFY_ROW_STATUS";
    public static final String EXTRA_VERIFY_ROW_CHECKSUM = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_VERIFY_ROW_CHECKSUM";
    public static final String EXTRA_VERIFY_CHECKSUM_STATUS = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_VERIFY_CHECKSUM_STATUS";
    public static final String EXTRA_SET_ACTIVE_APP = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_SET_ACTIVE_APP";
    public static final String EXTRA_VERIFY_APP_STATUS = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_VERIFY_APP_STATUS";
    public static final String EXTRA_VERIFY_EXIT_BOOTLOADER = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_VERIFY_EXIT_BOOTLOADER";
    public static final String EXTRA_ERROR_OTA = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_ERROR_OTA";

    //CYACD2 constants
    public static final String EXTRA_BTLDR_SDK_VER = "com.cypress.cysmart.backgroundservices." +
            "EXTRA_BTLDR_SDK_VER";

    /**
     * Shared Preference Status HandShake Status
     */
    public static final String PREF_BOOTLOADER_STATE = "PREF_BOOTLOADER_STATE";
    public static final String PREF_PROGRAM_ROW_NO = "PREF_PROGRAM_ROW_NO";
    public static final String PREF_PROGRAM_ROW_START_POS = "PREF_PROGRAM_ROW_START_POS";
    public static final String PREF_ARRAY_ID = "PREF_EXTRA_ARRAY_ID";
    /**
     * OTA File Selection Extras
     */
    public static final byte ACTIVE_APP_NO_CHANGE = -1;
    public static final long NO_SECURITY_KEY = -1;
    public static final int SECURITY_KEY_SIZE = 6;

    public static final String PREF_IS_CYACD2_FILE = "PREF_IS_CYACD2_FILE";
}
