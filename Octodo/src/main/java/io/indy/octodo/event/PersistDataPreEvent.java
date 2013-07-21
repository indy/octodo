package io.indy.octodo.event;

public class PersistDataPreEvent {

    private String mJSONFilename;

    public PersistDataPreEvent(String jsonFilename) {
        mJSONFilename = jsonFilename;
    }

    public String getJSONFilename() {
        return mJSONFilename;
    }
}
