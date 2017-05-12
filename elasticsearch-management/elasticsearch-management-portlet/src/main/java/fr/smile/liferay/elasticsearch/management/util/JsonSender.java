package fr.smile.liferay.elasticsearch.management.util;

import org.json.JSONObject;

import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by cattez on 11/05/2017.
 */
public class JsonSender {

    /**
     * Default constructor.
     */
    private JsonSender() {
    }

    /**
     * Send JSON Object in resource response.
     * @param response the resource response
     * @param json the json object to send
     * @throws IOException exception that could occur with resource print writer
     */
    public static void send(final ResourceResponse response, JSONObject json) throws IOException {
        send(response, json.toString());
    }

    /**
     * Send Json in resource response.
     * @param response the resource response
     * @param json the json object to send
     * @throws IOException exception that could occur with resource print writer
     */
    public static void send(final ResourceResponse response, String json) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(json);
    }

}
