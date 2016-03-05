package com.waytta;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class Utils {
    private static final String RETCODE_FIELD_NAME = "retcode";

    // Thinger to connect to saltmaster over rest interface
    public static JSONObject getJSON(String targetURL, JSONArray urlParams, String auth) {
        HttpURLConnection connection = null;
        JSONObject responseJSON = new JSONObject();

        try {
            // Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            if (urlParams != null && !urlParams.isEmpty()) {
                // We have stuff to send, so do an HTTP POST not GET
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

            }
            connection.setConnectTimeout(5000); // set timeout to 5 seconds
            if (auth != null && !auth.isEmpty()) {
                connection.setRequestProperty("X-Auth-Token", auth);
            }

            // Send request
            if (urlParams != null && !urlParams.isEmpty()) {
                // only necessary if we have stuff to send
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParams.toString());
                wr.flush();
                wr.close();
            }

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String responseText = response.toString();
            if (responseText.contains("java.io.IOException")
                    || responseText.contains("java.net.SocketTimeoutException")) {
                responseJSON.put("Error", responseText);
                return responseJSON;
            }
            try {
                // Server response should be json so this should work
                responseJSON = (JSONObject) JSONSerializer.toJSON(responseText);
                return responseJSON;
            } catch (Exception e) {
                responseJSON.put("Error", e);
                return responseJSON;
            }
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            responseJSON.put("Error", errors.toString());
            return responseJSON;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String getToken(String servername, JSONArray auth) {
        String token = new String();
        JSONObject httpResponse = getJSON(servername + "/login", auth, null);
        try {
            JSONArray returnArray = httpResponse.getJSONArray("return");
            for (Object o : returnArray) {
                JSONObject line = (JSONObject) o;
                // This token will be used for all subsequent connections
                token = line.getString("token");
            }
        } catch (Exception e) {
            token = "Auth Error: " + e + "\n\n" + httpResponse.toString(2).split("\\\\n")[0];
            return token;
        }
        return token;
    }

    // replaces $string with value of env($string). Used in conjunction with
    // parameterized builds
    public static String paramorize(AbstractBuild build, BuildListener listener, String paramer) {
        String expanded = paramer;
        try {
            final EnvVars env = build.getEnvironment(listener);
            expanded = env.expand(paramer);
            return expanded;
        } catch (InterruptedException | IOException e) {
           listener.getLogger().println("Error expanding " + paramer + ":" + e);
        }
        return expanded;
    }

    public static boolean validateFunctionCall(JSONArray returnArray) {
        boolean result = true;

        for (Object o : returnArray) {
            if (o instanceof Boolean) {
		result = (Boolean) o;
	    } else if (o instanceof String){
		result = false;
	    } else {
                result = validateInnerJsonObject((JSONObject) o);
                if (!result) {
                    break;
                }
	    }
        }

        return result;
    }

    private static boolean validateInnerJsonObject(JSONObject minion) {
        boolean result = true;

        for (Object name : minion.names()) {
            Object field = minion.get(name.toString());

            if (field instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) field;

                if (jsonObject.has(RETCODE_FIELD_NAME)) {
                    result = jsonObject.getInt(RETCODE_FIELD_NAME) == 0;

                    if (!result) {
                        break;
                    }
                }
                if (jsonObject.has("result")) {
                    result = jsonObject.getBoolean("result");

                    if (!result) {
                        break;
                    }
                }

                result = validateInnerJsonObject(jsonObject);

                if (!result) {
                    break;
                }
            }
        }

        return result;
    }
}
