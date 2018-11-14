package com.cypress.cysmart.OTAFirmwareUpdate;

import android.util.Log;

import com.cypress.cysmart.CommonUtils.Utils;
import com.cypress.cysmart.DataModelClasses.OTAFlashRowModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CustomFileReader {
    private final File mFile;
    private FileReadStatusUpdater mFileReadStatusUpdaterUpdater;
    private final String mHeader;
    private int mReadingLine;
    private String mSiliconID;

    public CustomFileReader(String filepath) {
        this.mReadingLine = 0;
        this.mFile = new File(filepath);
        this.mHeader = getTheHeaderString(this.mFile);
        Log.e("CustomFileReader", "PATH>>>" + filepath);
    }

    public void setFileReadStatusUpdater(FileReadStatusUpdater fileReadStatusUpdater) {
        this.mFileReadStatusUpdaterUpdater = fileReadStatusUpdater;
    }

    public String[] analyseFileHeader() {
        String[] headerData = new String[3];
        String MSBString = Utils.getMSB(this.mHeader);
        this.mSiliconID = getSiliconID(MSBString);
        String mSiliconRev = getSiliconRev(MSBString);
        String mCheckSumType = getCheckSumType(MSBString);
        headerData[0] = this.mSiliconID;
        headerData[1] = mSiliconRev;
        headerData[2] = mCheckSumType;
        return headerData;
    }

    public ArrayList<OTAFlashRowModel> readDataLines() {
        ArrayList<OTAFlashRowModel> flashDataLines = new ArrayList();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.mFile));
            while (true) {
                String dataLine = bufferedReader.readLine();
                if (dataLine == null) {
                    break;
                }
                this.mReadingLine++;
                this.mFileReadStatusUpdaterUpdater.onFileReadProgressUpdate(this.mReadingLine);
                OTAFlashRowModel model = new OTAFlashRowModel();
                if (this.mReadingLine != 1) {
                    StringBuilder dataBuilder = new StringBuilder(dataLine);
                    dataBuilder.deleteCharAt(0);
                    model.mArrayId = Integer.parseInt(dataBuilder.substring(0, 2), 16);
                    model.mRowNo = Utils.getMSB(dataBuilder.substring(2, 6));
                    model.mDataLength = Integer.parseInt(dataBuilder.substring(6, 10), 16);
                    model.mRowCheckSum = Integer.parseInt(dataBuilder.substring(dataLine.length() - 3, dataLine.length() - 1), 16);
                    String datacharacters = dataBuilder.substring(10, dataLine.length() - 2);
                    byte[] data = new byte[model.mDataLength];
                    int i = 0;
                    int j = 0;
                    while (i < model.mDataLength) {
                        data[i] = (byte) Integer.parseInt(datacharacters.substring(j, j + 2), 16);
                        i++;
                        j += 2;
                    }
                    model.mData = data;
                    flashDataLines.add(model);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return flashDataLines;
    }

    public int getTotalLines() {
        int totalLines = 0;
        String dataLine = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(mFile));
            while ((dataLine = bufferedReader.readLine()) != null) {
                totalLines++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalLines;
    }

    private String getTheHeaderString(File file) {
        String header = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            header = bufferedReader.readLine();
            bufferedReader.close();
            return header;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return header;
        } catch (IOException e2) {
            e2.printStackTrace();
            return header;
        }
    }

    private String getSiliconID(String header) {
        return header.substring(4, 12);
    }

    private String getSiliconRev(String header) {
        return header.substring(2, 4);
    }

    private String getCheckSumType(String header) {
        return header.substring(0, 2);
    }
}