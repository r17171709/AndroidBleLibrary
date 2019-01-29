/*
 * Copyright Cypress Semiconductor Corporation, 2014-2018 All rights reserved.
 *
 * This software, associated documentation and materials ("Software") is
 * owned by Cypress Semiconductor Corporation ("Cypress") and is
 * protected by and subject to worldwide patent protection (UnitedStates and foreign),
 * United States copyright laws and international
 * treaty provisions. Therefore, unless otherwise specified in a separate
 * license agreement between you and Cypress, this Software
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

package com.cypress.cysmart.OTAFirmwareUpdate;

/**
 * Class created for bootloader command constants
 */
class BootLoaderCommands_v1 {

    /* Command identifier for verifying the checksum value of the bootloadable project. */
    public static final int VERIFY_APP = 0x31;
    /* Command identifier for sending a block of data to the bootloader without doing anything with it yet. */
    public static final int SEND_DATA = 0x37;
    /* Command identifier for starting the boot loader.  All other commands ignored until this is sent. */
    public static final int ENTER_BOOTLOADER = 0x38;
    /* Command identifier for exiting the bootloader and restarting the target program. */
    public static final int EXIT_BOOTLOADER = 0x3B;
    /* Command identifier for sending a block of data to the bootloader without doing anything with it yet. */
    public static final int SEND_DATA_WITHOUT_RESPONSE = 0x47;
    /* Command to program data. */
    public static final int PROGRAM_DATA = 0x49;
    /* Command to verify data */
    public static final int VERIFY_DATA = 0x4A;
    /* Command to set application metadata in bootloader SDK */
    public static final int SET_APP_METADATA = 0x4C;
    /* Command to set encryption initial vector */
    public static final int SET_EIV = 0x4D;

    public static final int PACKET_START = 0x01;
    public static final int PACKET_END = 0x17;
    public static final int BASE_CMD_SIZE = 7;//SOP(1) + CmdCode(1) + DataLength(2) + Checksum(2) + EOP(1)
    public static final int WRITE_WITH_RESP_MAX_DATA_SIZE = 133;
    public static final int WRITE_NO_RESP_MAX_DATA_SIZE = 300;
}
