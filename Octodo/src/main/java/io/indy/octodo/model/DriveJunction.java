/*
 * Copyright 2013 Inderjit Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    static private final boolean D = true;
    static private final String TAG = DriveJunction.class.getSimpleName();

    static void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

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
            ifd("about to insert file");
            file = service.files().insert(config, content).execute();
            ifd("file returned: " + file);

        } catch (IOException e) {
            ifd("createAppDataJsonFile exception: " + e);
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
            ifd("updateAppDataJsonFile exception: " + e);
            throw e;
        }
        return file;
    }

    public static File getFileMetadata(Drive service, String fileId) throws IOException {
        File f;
        try {
            f = service.files().get(fileId).execute();
        } catch (IOException e) {
            ifd("(0/3) getFileMetadata params: fileId=" + fileId);
            ifd("(1/3) getFileMetadata exception: " + e);
            ifd("(2/3) getFileMetadata exception: " + e.getLocalizedMessage());
            ifd("(3/3) getFileMetadata exception: " + e.getMessage());
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
                ifd("downloadFile exception: " + e);
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
            ifd("downloadFileAsString exception: " + e);
            throw e;
        }

        return res;
    }
}
