package io.indy.octodo.model;

import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DriveJunction {

    private final static String TAG = "DriveJunction";

    private final static String JSON_MIMETYPE = "application/json";
    private final static String APPDATA_FOLDER = "appdata";

    public static List<File> listAppDataFiles(Drive service) throws IOException {

        List<File> result = new ArrayList<File>();
        Drive.Files.List request = service.files().list();
        request.setQ("'appdata' in parents");

        do {
            try {
                FileList files = request.execute();
                result.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                Log.d("MainActivity", "An error occurred: " + e);
                request.setPageToken(null);
                throw e;
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    // in practice the appdata folder will always contain json files
    public static File createAppDataJsonFile(Drive service, String title, String json) throws IOException {

        ByteArrayContent content = ByteArrayContent.fromString(JSON_MIMETYPE, json);

        List<ParentReference> parents = new ArrayList<ParentReference>();
        parents.add(new ParentReference().setId(APPDATA_FOLDER));

        File config = new File();
        config.setTitle(title);
        config.setParents(parents);

        File file;
        try {
            file = service.files().insert(config, content).execute();
        } catch (IOException e) {
            Log.d(TAG, "createAppDataJsonFile exception: " + e);
            throw e;
        }
        return file;
    }

    public static File updateAppDataJsonFile(Drive service, File metadata, String json) throws IOException {
        ByteArrayContent content = ByteArrayContent.fromString(JSON_MIMETYPE, json);

        File file;
        try {
            file = service.files().update(metadata.getId(), metadata, content).execute();
        } catch (IOException e) {
            Log.d(TAG, "updateAppDataJsonFile exception: " + e);
            throw e;
        }
        return file;
    }

    public static File getFileMetadata(Drive service, String fileId) throws IOException {
        File f;
        try {
            f = service.files().get(fileId).execute();
        } catch (IOException e) {
            Log.d(TAG, "getFileMetadata exception: " + e);
            throw e;
        }
        return f;
    }

    public static InputStream downloadFile(Drive service, File file) throws IOException {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                HttpResponse resp =
                        service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
                                .execute();
                return resp.getContent();
            } catch (IOException e) {
                // An error occurred.
                Log.d(TAG, "downloadFile exception: " + e);
                throw e;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }

    public static String downloadFileAsString(Drive service, File file) throws IOException {

        String res = null;

        try {
            InputStream is = DriveJunction.downloadFile(service, file);

            if (is != null) {

                InputStreamReader isr = new InputStreamReader(is);
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(isr);
                String read = br.readLine();

                while (read != null) {
                    sb.append(read);
                    read = br.readLine();

                }

                res = sb.toString();
            }
        } catch (IOException e) {
            Log.d(TAG, "downloadFileAsString exception: " + e);
            throw e;
        }

        return res;
    }
}
