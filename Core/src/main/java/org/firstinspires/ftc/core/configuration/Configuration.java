/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Configuration management class
   ------------------------------------------------------- */
package org.firstinspires.ftc.core.configuration;

/* System includes */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

// Json includes
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

/* Android includes */
import android.os.Environment;

// Core includes
import org.firstinspires.ftc.core.realtime.Logger;

public class Configuration {

    // Status
    boolean                                 mIsValid;

    // Registered elements to read and write configuration
    final Map<String, ConfigurableItem>     mConfigRegistry;
    JSONObject                              mContent;

    // Loggers
    Logger                                  mLogger;
    String                                  mFilename;

    /**
     * Configuration constructor
     *
     */
    public Configuration() {

        mConfigRegistry = new LinkedHashMap<>();
        mIsValid        = false;
        mContent        = null;

    }

    /**
     * Configuration logger association
     *
     * @param logger logger
     */
    public void logger(Logger logger) {
        mLogger         = logger;
    }

    /**
     * Registers an object to be configured
     *
     * @param key configuration key corresponding to the object configuration
     * @param element object to configure
     */
    public void register(String key, ConfigurableItem element) {
        mConfigRegistry.put(key, element);
    }

    /**
     * Checks if configuration is valid
     *
     * @return true if configuration is valid, false otherwise
     */
    public boolean isValid() { return mIsValid; }


    /**
     * Reads configuration from the default configuration file
     *
     */
    public void read(String name) {

        String filename = Environment.getExternalStorageDirectory().getPath()
                + "/FIRST/"
                + name
                + ".json";
        this.readJson(filename);
    }

    /**
     * Reads configuration from a given configuration file
     *
     * @param filename : filename containing configuration
     */
    public void readJson(String filename) {

        mFilename = Configuration.getRawFilename(filename);
        mIsValid  = true;

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        catch(IOException e) {
            mLogger.error("Configuration file " + filename + " reading failed");
            mIsValid = false;
        }

        // Parse JSON content
        mContent = null;
        try {
            mContent = new JSONObject(content.toString());
        }
        catch(JSONException e) {
            mLogger.error("Configuration file " + filename + " is not json formatted");
            mIsValid = false;
        }

        if(mIsValid) {

            // Loop into element to load their configuration
            for (Map.Entry<String, ConfigurableItem> element : mConfigRegistry.entrySet()) {

                try {

                    // Split key between keys and indexes
                    String[] keys = element.getKey().split("[.\\[\\]]");
                    Object data = mContent;
                    for (String key : keys) {

                        if (!key.isEmpty()) {

                            if (data instanceof JSONObject) {
                                data = ((JSONObject) data).get(key);
                            } else if (data instanceof JSONArray) {
                                int index = Integer.parseInt(key);
                                data = ((JSONArray) data).get(index);
                            }
                        }

                    }

                    if (data instanceof JSONObject) {
                        element.getValue().read((JSONObject) data);
                    } else {
                        mLogger.error("Target key " + element.getKey() + " does not lead to an object" );
                        mIsValid = false;
                    }
                }
                catch (JSONException | NumberFormatException e) {
                    mLogger.error("Configuration " + element.getKey() + " can not be read" );
                    mIsValid = false;
                }
            }

        }
        for (Map.Entry<String, ConfigurableItem> element : mConfigRegistry.entrySet()) {
            if(!element.getValue().isConfigured()) {
                mLogger.error("Configuration " + element.getKey() + " is not valid" );
                mIsValid = false;
            }
        }
    }

    /**
     * Writes configuration to a given configuration file
     *
     * @param filename : name of the file to write
     */
    public void write(String filename) {

        JSONObject configuration = new JSONObject();

        // Loop into element to write their configuration
        for (Map.Entry<String, ConfigurableItem> element : mConfigRegistry.entrySet()) {

            if(element.getValue().isConfigured()) {

                try {
                    // Split key between topics - Indexes still belong to the key they come from,
                    String[] keys = element.getKey().split("[.]");
                    Object data = configuration;
                    for (String key : keys) {

                        if (!key.isEmpty()) {

                            if (data == null) { throw new RuntimeException(); }

                            JSONObject json = ((JSONObject) data);

                            String k;
                            List<Integer> indexes = new ArrayList<>();
                            k = Configuration.getKeyAndIndexes(key, indexes, mLogger);

                            if (!json.has(k) && indexes.isEmpty())  { json.put(k, new JSONObject()); }
                            if (!json.has(k) && !indexes.isEmpty()) { json.put(k, new JSONArray());  }

                            Object temp = json.get(k);
                            if ((temp instanceof JSONObject) && (!indexes.isEmpty())) {
                                throw new RuntimeException();
                            }
                            if ((temp instanceof JSONArray) && (indexes.isEmpty())) {
                                throw new RuntimeException();
                            }

                            if (indexes.isEmpty()) {
                                if (!(temp instanceof JSONObject)) {  throw new RuntimeException(); }
                                data = temp;
                            } else {
                                if (!(temp instanceof JSONArray)) {  throw new RuntimeException(); }
                                JSONArray array = ((JSONArray) temp);

                                for (int i_index = 0; i_index < (indexes.size() - 1); i_index++) {
                                    if (array.length() <= indexes.get(i_index)) {
                                        for (int j_index = array.length(); j_index <= indexes.get(i_index); j_index++) {
                                            array.put(new JSONArray());
                                        }
                                    }

                                    temp = array.get(indexes.get(i_index));
                                    array = ((JSONArray) temp);
                                }
                                if (array.length() <= indexes.get(indexes.size() - 1)) {
                                    for (int j_index = array.length(); j_index <= indexes.get(indexes.size() - 1); j_index++) {
                                        array.put(new JSONObject());
                                    }

                                    temp = array.get(indexes.get(indexes.size() - 1));
                                    data = temp;
                                }
                            }
                        }
                    }
                    element.getValue().write((JSONObject) data);
                } catch (RuntimeException | JSONException e) {
                    mLogger.error("Configuration formatting failed");
                }
            }
        }

        // Write JSON to file
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8));
            writer.write(configuration.toString(4)); // 4 is the indentation level for pretty printing
            writer.close();
        }
        catch(JSONException | IOException e) { mLogger.error("Unable to write json file"); }
    }

    /**
     * Log configuration content
     */
    public void log() {

        // Log to driver station
        if(mIsValid) {
            mLogger.raw(Logger.Target.DRIVER_STATION, "CNF " + mFilename + " is valid\n");
        }
        else {
            mLogger.raw(Logger.Target.DRIVER_STATION, "CNF " + mFilename + " is not valid\n");
        }

        // Log to dashboard
        StringBuilder confstring = new StringBuilder();
        confstring.append("-------------------------\n");
        if(mIsValid) {
            confstring.append("<p style=\"color: green; font-size: 14px\"> Conf ")
                    .append(mFilename)
                    .append(" is valid</p>");
        }
        else {
            confstring.append("<p style=\"color: red; font-size: 14px\"> Conf ")
                    .append(mFilename)
                    .append(" is not valid</p>");
        }

        // Loop into element to write their configuration
        for (Map.Entry<String, ConfigurableItem> element : mConfigRegistry.entrySet()) {

            confstring.append("-------------------------\n")
                    .append("<details>\n")
                    .append("<summary style=\"font-size: 12px; font-weight: 500\"> ")
                    .append(element.getKey().toUpperCase())
                    .append(" </summary>\n")
                    .append("<ul>\n")
                    .append(element.getValue().logConfigurationText(""))
                    .append("</ul>\n")
                    .append("</details>\n");

        }

        mLogger.raw(Logger.Target.DASHBOARD,confstring.toString());

        // Log to file
        confstring = new StringBuilder();
        confstring.append("\n-------------------------\n");
        if(mIsValid) {
            confstring.append(mFilename)
                    .append(" is valid\n");
        }
        else {
            confstring.append(mFilename)
                    .append(" is not valid\n");
        }
        // Loop into element to write their configuration
        for (Map.Entry<String, ConfigurableItem> element : mConfigRegistry.entrySet()) {

            confstring.append("-------------------------\n")
                    .append(element.getKey().toUpperCase())
                    .append("\n")
                    .append(element.getValue().logConfigurationText("--"))
                    .append("\n");

        }

        mLogger.raw(Logger.Target.SYSTEM,confstring.toString());
    }

    /**
     * Look for a configuration topic
     *
     * @param topic Topic to look for
     * @return Topic content
     */
    public JSONObject                   search(String topic){

        JSONObject result = null;
        if(mIsValid) {
            try{

                // Split topic between keys and indexes
                String[] keys = topic.split("[.\\[\\]]");
                Object data = mContent;
                for (String key : keys) {

                    if (!key.isEmpty()) {

                        if (data instanceof JSONObject) {
                            data = ((JSONObject) data).get(key);
                        } else if (data instanceof JSONArray) {
                            int index = Integer.parseInt(key);
                            data = ((JSONArray) data).get(index);
                        }
                    }
                }

                result = (JSONObject) data;

            }
            catch (JSONException | NumberFormatException e) {
                mLogger.error("Configuration " + topic + " can not be read" );
                mIsValid = false;
            }
        }
        return result;
    }

    /**
     * Extract the raw filename, without path or extension
     *
     * @param filename The input filename
     * @return The extracted filename
     */
    private static String               getRawFilename(String filename) {
        String result;

        File file = new File(filename);
        result = file.getName();

        int dotIndex = result.lastIndexOf(".");
        result = result.substring(0,dotIndex);

        return result;
    }

    /**
     * Separate a JSON entry between keys (JSONObjects) and indexes (JSONArrays)
     * ex : data[0][1][4] will return data and update indexes with 0,1 and 4
     *
     * @param key The input entry
     * @param indexes The list of indexes to update
     * @param logger the logger to use for error logging
     * @return The extracted entry topic
     */
    private static String                  getKeyAndIndexes(String key, List<Integer> indexes, Logger logger) {

        String result = key;
        indexes.clear();

        if (key.contains("[") && key.contains("]")) {
            result = key.substring(0, key.indexOf("["));
            String[] inds = key.substring(key.indexOf("["), key.length() - 1).split("[\\[\\]]");

            for (String index : inds) {
                if(!index.isEmpty()) {
                    try {
                        Integer i = Integer.parseInt(index);
                        indexes.add(i);
                    }
                    catch(NumberFormatException e) {
                        logger.error(key + " does contains an invalid index");
                    }
                }
            }
        }

        return result;

    }


}
