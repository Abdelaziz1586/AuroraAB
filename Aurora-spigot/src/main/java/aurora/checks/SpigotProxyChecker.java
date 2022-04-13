package aurora.checks;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class SpigotProxyChecker {

    static HashMap<String, JSONObject> IPInfo = new HashMap<>();

    public static boolean isProxy(String IP) {
        return IPInfo.get(IP).getBoolean("proxy");
    }

    public static String getCountry(String IP) {
        return IPInfo.get(IP).getString("country");
    }

    public static void saveIPInfo(String IP) throws IOException {
        if (IPInfo.containsKey(IP)) return;
        InputStream inputStream;
        URL url = new URL("http://ip-api.com/json/" + IP + "?fields=country,proxy");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestProperty("Accept", "application/json");
        int responseCode = http.getResponseCode();
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = http.getInputStream();
        } else {
            inputStream = http.getErrorStream();
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String currentLine;
        while ((currentLine = in.readLine()) != null)
            response.append(currentLine);
        in.close();
        JSONObject object = new JSONObject(response.toString());
        IPInfo.put(IP, object);
    }

}
