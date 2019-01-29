package com.cypress.cysmart.OTAFirmwareUpdate;

import android.os.Bundle;

public interface OTAFUHandler {
    public void setPrepareFileWriteEnabled(boolean enabled);

    public void prepareFileWrite();

    public void processOTAStatus(String status, Bundle extras);
}
