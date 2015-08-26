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

package edu.uri.egr.hermes.manipulators;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.uri.egr.hermes.Hermes;
import edu.uri.egr.hermes.exceptions.HermesException;
import timber.log.Timber;

public class FileLog {
    private File file;
    private String[] headers;
    private String[] valuePool;

    public FileLog(String name, String path) {
        this.file = Hermes.get().getFileWrapper().createExternal(name, path);
    }

    public FileLog(String name) {
        this(name, "");
    }

    public void setHeaders(String... headers) {
        this.headers = headers;
        valuePool = new String[headers.length];

        generateFile();
    }

    public void writeSpecific(int column, Object value) {
        if (column >= valuePool.length)
            throw new HermesException("Attempting to write a value to an unavailable column index. Column size is " + valuePool.length + " and you accessed " + column);

        valuePool[column] = String.valueOf(value);
    }

    public void write(Object... data) {
        if (data.length > valuePool.length)
            throw new HermesException("Attempting to write values to an unavailable column index.  Column size is " + valuePool.length + " and you passed " + data.length + " objects.");

        for (int i = 0; i < data.length; i++) {
            valuePool[i] = String.valueOf(data[i]);
        }

        if (data.length == valuePool.length)
            flush();
        else
            Timber.i("Incomplete log write.  You wrote %d entries, but the line holds %d!", data.length, valuePool.length);
    }

    public void flush() {
        try {
            CSVPrinter printer = CSVFormat.EXCEL
                    .print(new FileWriter(file, true));

            printer.printRecord(valuePool);
            printer.close();

            Hermes.get().getFileWrapper().makeVisible(file);
        } catch (IOException e) {
            Timber.e("Failed to write log file: %s", e.getMessage());
        }
    }

    public String time() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    public String msTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    public String msTimeFrom(long ms) {
        return String.valueOf(System.currentTimeMillis() - ms);
    }

    public String date() {
        return new SimpleDateFormat("MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private void generateFile() {
        if (!file.exists()) {
            writeHeaders();
        } else {
            if (!headerIsEqual(readHeaders())) {
                Timber.d("Headers changed - deleting log %s", file.getName());

                file.delete();
                generateFile();
            }
        }
    }

    private boolean headerIsEqual(String[] read) {
        if (read.length != headers.length)
            return false;

        for (int i = 0; i < headers.length; i++) {
            if (!read[i].equals(headers[i]))
                return false;
        }

        return true;
    }

    private String[] readHeaders() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String header = reader.readLine();

            reader.close();

            return header.split(",");
        } catch (IOException e) {
            Timber.e("Failed to read headers: %s", e.getMessage());
            return new String[0];
        }
    }

    private void writeHeaders() {
        try {
            CSVFormat.EXCEL
                    .withHeader(headers)
                    .print(new FileWriter(file))
                    .close();
        } catch (IOException e) {
            Timber.e("Failed to log: %s", e.getMessage());
        }
    }
}
