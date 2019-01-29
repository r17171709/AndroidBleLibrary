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
 * Class created for bootloader commands constants
 */
class BootLoaderCommands_v0 {

    /* Command identifier for verifying the checksum value of the bootloadable project. */
    public static final int VERIFY_CHECK_SUM = 0x31;
    /* Command identifier for getting the number of flash rows in the target device. */
    public static final int GET_FLASH_SIZE = 0x32;
    /* Command identifier for getting info about the app status. This is only supported on multi app bootloader. */
    public static final int GET_APP_STATUS = 0x33;
    /* Command identifier for setting the active application. This is only supported on multi app bootloader. */
    public static final int SET_ACTIVE_APP = 0x36;
    /* Command identifier for sending a block of data to the bootloader without doing anything with it yet. */
    public static final int SEND_DATA = 0x37;
    /* Command identifier for starting the boot loader.  All other commands ignored until this is sent. */
    public static final int ENTER_BOOTLOADER = 0x38;
    /* Command identifier for programming a single row of flash. */
    public static final int PROGRAM_ROW = 0x39;
    /* Command to verify data */
    public static final int VERIFY_ROW = 0x3A;
    /* Command identifier for exiting the bootloader and restarting the target program. */
    public static final int EXIT_BOOTLOADER = 0x3B;

    public static final int PACKET_START = 0x01;
    public static final int PACKET_END = 0x17;
    public static final int BASE_CMD_SIZE = 0x07;//SOP(1) + CmdCode(1) + DataLength(2) + Checksum(2) + EOP(1)
    public static final int WRITE_WITH_RESP_MAX_DATA_SIZE = 133;
    public static final int WRITE_NO_RESP_MAX_DATA_SIZE = 300;
}
