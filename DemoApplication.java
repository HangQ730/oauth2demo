/**
* Oauth2 Demo with SpingBoot
* @author Hang
*/
import java.util.regex.*;
import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Controller
public class DemoApplication {

    private final String CLIENT_ID = "<your_client_id>";
    private final String CLIENT_SECRET = "<your_client_secret>";
    private final String REDIRECT_URL = "<your_call_back>"; //This url need to be configured in the application's callback url

    private final String PORTAL_URL = "<your_oauth_server_url>"; // Oauth2 server
    private final String OAUTH2_AUTHORIZE_URL = PORTAL_URL + "/api/oauth2/authorize";
    private final String OAUTH2_TOKEN_URL = PORTAL_URL + "/api/oauth2/token";
    private final String OAUTH2_USERINFO_URL = PORTAL_URL + "/api/oauth2/userinfo";

    
    public String getuserinfo(String token) {
        String respon = sendGet(OAUTH2_USERINFO_URL, token);
        System.out.println("*********GET USERINFO*********");
        System.out.println(respon);
        return respon;
    }

    public String getAccessToken(String code) {
        String params = "grant_type=authorization_code&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
                "&code=" + code + "&redirect_uri=" + REDIRECT_URL;
        String respon = sendPost(OAUTH2_TOKEN_URL, params);
        System.out.println("*********GET TOKEN**********");
        System.out.println(respon);
        // Get token
        Pattern p = Pattern.compile("\\{\"access_token\":\"(.*?)\",");
        Matcher matcher = p.matcher(respon);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            return matcher.group(1);
        }
        return respon;
    }

    private String sendGet(String url, String token) {
        String result = "";
        BufferedReader bufferedReader = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
            for (String key : map.keySet()) {
                System.out.println(key + ":" + map.get(key));
            }

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line = "";
            while (null != (line = bufferedReader.readLine())) {
                result += line;
            }

        } catch (Exception e) {
            System.out.println("Error occurs when send GET request: " + e);
            e.printStackTrace();
        } finally {  
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    private String sendPost(String url, String param) {
        String result = "";
        BufferedReader bufferedReader = null;
        PrintWriter out = null;
        try {
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            out = new PrintWriter(connection.getOutputStream());
            out.print(param);
            out.flush();
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            while (null != (line = bufferedReader.readLine())) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("Error occurs when send POST request:" + e);
            e.printStackTrace();
        } finally { 
            try {
                if (null != out) {
                    out.close();
                }
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    // This is the address of the application you want to access. 
    // When the browser accesses it, it automatically requests the Oauth2 server for the code and then redirect to the callback url
    @RequestMapping("/") 
    public String redirect() {
        String redirect_url = "redirect:" + OAUTH2_AUTHORIZE_URL + "?response_type=code&" + "client_id=" +
                CLIENT_ID + "&redirect_uri=" + REDIRECT_URL;
        return redirect_url;
    }

    // This is the url to receive the login information. In this Demo, it is only used to print the login information.
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String call_back(@RequestParam("code") String code) {
        // Print the json
        System.out.println(code);
        String access_token = getAccessToken(code);
        String userinfo = getuserinfo(access_token);
        JSONObject result = new JSONObject();
        result.put("msg", "ok");
        result.put("method", "json");
        result.put("data", userinfo);
        return result.toJSONString();
    }


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
