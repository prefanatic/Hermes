/*
 * Copyright 2015 Cody Goldberg
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

package edu.uri.egr.hermes.wrappers;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
import timber.log.Timber;

public class FileWrapper {
    private final Hermes hermes;

    public FileWrapper(Hermes hermes) {
        this.hermes = hermes;
    }

    /**
     * Creates a file based on a specific path and name, inside the root folder.
     * @param name The name of the file.
     * @param path The location of the file.
     * @return File
     */
    public File create(String name, String path) {
        File base = new File(hermes.getRootFolder(), path);
        if (base.mkdirs())
            Timber.d("Making directories to %s", path);

        return new File(base, name);
    }

    /**
     * Creates a file based on a specific name, and stores it in the configured base directory.
     * @param name The name of the file.
     * @return File
     */
    public File create(String name) {
        File base = hermes.getRootFolder();
        return new File(base, name);
    }

    /**
     * Creates a temporary file on the internal storage.
     * @param name Name of the temp file.
     * @return File
     */
    public File createCache(String name) {
        return new File(hermes.getContext().getCacheDir(), name);
    }

    /**
     * Creates a file on the external storage device, using the specified path and name.
     * @param name The name of the file.
     * @param path The location of the file, on the external storage device.
     * @return File
     */
    public File createExternal(String name, String path) {
        File base = new File(Environment.getExternalStorageDirectory(), path);
        if (base.mkdirs())
            Timber.d("Making directories to %s", path);

        return new File(base, name);
    }

    /**
     * Creates a file on the external storage device, using a specified name.
     * @param name The name of the file.
     * @return File
     */
    public File createExternal(String name) {
        return createExternal(name, "");
    }

    /**
     * Makes the file visible to MTP - so you can see it when you connect to your computer.
     * @param file The file.
     */
    public void makeVisible(File file) {
        Intent mediaScanner = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanner.setData(Uri.fromFile(file));
        hermes.getContext().sendBroadcast(mediaScanner);
    }
}
