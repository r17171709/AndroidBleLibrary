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

import com.cypress.cysmart.CommonUtils.Logger;
import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.DataModelClasses.OTAFlashRowModel_v0;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class created to read the .cycad files.The read file is stored temporarily
 */
class CustomFileReader_v0 {
    private String mSiliconID;
    private final String mHeader;
    private final File mFile;
    private int mReadingLine = 0;

    //File read status updater
    private FileReadStatusUpdater mFileReadStatusUpdater;

    //Constructor
    public CustomFileReader_v0(String filepath) {
        mFile = new File(filepath);
        mHeader = getHeader(mFile);
        Logger.e("PATH>>>"+filepath);
    }

    public void setFileReadStatusUpdater(FileReadStatusUpdater fileReadStatusUpdater) {
        this.mFileReadStatusUpdater = fileReadStatusUpdater;
    }

    /**
     * Analysing the header file and extracting the silicon ID,Check Sum Type and Silicon rev
     */
    public String[] analyseFileHeader() {
        String[] headerData = new String[3];
        String MSBString = Utils.getMSB(mHeader);
        mSiliconID = getSiliconID(MSBString);
        String siliconRev = getSiliconRev(MSBString);
        String checkSumType = getCheckSumType(MSBString);
        headerData[0] = mSiliconID;
        headerData[1] = siliconRev;
        headerData[2] = checkSumType;
        return headerData;
    }

    /**
     * Method to parse the file a read each line and put the line to a data model
     *
     * @return
     */
    public ArrayList<OTAFlashRowModel_v0> readDataLines() {
        ArrayList<OTAFlashRowModel_v0> flashDataLines = new ArrayList<>();
        String dataLine = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(mFile));
            while ((dataLine = bufferedReader.readLine()) != null) {
                mReadingLine++;
                mFileReadStatusUpdater.onFileReadProgressUpdate(mReadingLine);
                byte[] data;

                OTAFlashRowModel_v0 model = new OTAFlashRowModel_v0();
                if (mReadingLine != 1) {
                    StringBuilder sb = new StringBuilder(dataLine);
                    sb.deleteCharAt(0);
                    model.mArrayId = Integer.parseInt(sb.substring(0, 2), 16);
                    model.mRowNo = Utils.getMSB(sb.substring(2, 6));
                    model.mDataLength = Integer.parseInt(sb.substring(6, 10), 16);
                    model.mRowCheckSum = Integer.parseInt(sb.substring(dataLine.length() - 3, dataLine.length() - 1), 16);
                    String dataString = sb.substring(10, dataLine.length() - 2);
                    data = new byte[model.mDataLength];
                    for (int i = 0, j = 0; i < model.mDataLength; i++, j += 2) {
                        data[i] = (byte) Integer.parseInt(dataString.substring(j, j + 2), 16);
                    }
                    model.mData = data;
                    flashDataLines.add(model);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flashDataLines;
    }

    /**
     * Count the number of lines in the selected file
     *
     * @return totalLines
     */
    public int getTotalLines() {
        int totalLines = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mFile));
            try {
                String dataLine = null;
                while ((dataLine = reader.readLine()) != null) {
                    totalLines++;
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalLines;
    }

    /**
     * Read the first line from the file
     *
     * @param file
     * @return
     */
    protected String getHeader(File file) {
        String header = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                header = reader.readLine();
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }

    private String getSiliconID(String header) {
        String siliconID = header.substring(4, 12);
        return siliconID;
    }

    private String getSiliconRev(String header) {
        String siliconRev = header.substring(2, 4);
        return siliconRev;
    }

    private String getCheckSumType(String header) {
        String checkSumType = header.substring(0, 2);
        return checkSumType;
    }
}
