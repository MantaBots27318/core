package org.firstinspires.ftc.core.configuration;

/* Json includes */
import org.json.JSONObject;

public interface ConfigurableItem {

    /**
     * Reads configuration from a JSON object
     *
     * @param reader : JSON object containing configuration
     */
    void read(JSONObject reader);

    /**
     * Writes configuration to a JSON object
     *
     * @param writer : JSON object to write configuration includes
     */
    void write(JSONObject writer);

    /**
     * Configuration checking
     *
     * @return true if object is correctly configured, false otherwise
     */
    boolean isConfigured();


    /**
     * Configuration logging into string
     *
     * @return configuration as string
     */
    String logConfigurationText(String header);

}

