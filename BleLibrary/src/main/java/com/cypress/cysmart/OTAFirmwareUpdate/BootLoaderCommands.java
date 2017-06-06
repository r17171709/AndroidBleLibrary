package com.cypress.cysmart.OTAFirmwareUpdate;

public class BootLoaderCommands {
    public static final int BASE_CMD_SIZE = 7;
    public static final int ENTER_BOOTLOADER = 56;
    public static final int EXIT_BOOTLOADER = 59;
    public static final int GET_FLASH_SIZE = 50;
    public static final int MAX_DATA_SIZE = 133;
    public static final int PACKET_END = 23;
    public static final int PROGRAM_ROW = 57;
    public static final int SEND_DATA = 55;
    public static final int VERIFY_CHECK_SUM = 49;
    public static final int VERIFY_ROW = 58;

    BootLoaderCommands() {
    }
}