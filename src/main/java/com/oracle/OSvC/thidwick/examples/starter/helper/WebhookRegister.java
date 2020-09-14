package com.oracle.OSvC.thidwick.examples.starter.helper;
import com.oracle.OSvC.thidwick.examples.starter.constants.Constants;
import com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyWebhookReg;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class WebhookRegister {

    RequestBodyWebhookReg request;
    public  WebhookRegister(RequestBodyWebhookReg request)
    {
        this.request = request;
    }

    public String registerWebhooks()
    {
       String webhookId = registerWebhooks(request.getInvocationEndpoint(), request.getEventId());
       if(webhookId != null && !webhookId.isEmpty())
       {
           return  webhookId;
       }
       else
       {
           return  "Webhook registration Failed";
       }
    }

    /* Creates an event with given event id,
       Creates a webhook with given invoke end point,
       Creates rules,
       Returns the created webhook id */
    private String registerWebhooks(String invokeEndPoint, String event) {
        String webhookId = "";
        try {
            // Get Authorization token
            String token = getAuthHeader();
            System.out.println(String.format("Authorization token %s", token));
            System.out.println();
            System.out.println();
            HttpClient client = HttpClients.createDefault();

            // Create event
            HttpPost httpPost = new HttpPost("https://authentication-service-integ-helios-qa2.channels.ocs.oc-test.com/resources/v1/events");
            httpPost.addHeader("Authorization", token);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            JSONObject eventName = new JSONObject();
            eventName.put("name", event);
            StringEntity entity = new StringEntity(String.valueOf(eventName), Charset.forName("UTF-8"));
            httpPost.setEntity(entity);
            HttpResponse response = client.execute(httpPost);
            String responseJSON = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(responseJSON);
            String eventId = (String) jsonObj.get("id");
            System.out.printf("Created Event with id %s\n", eventId);
            System.out.println();
            System.out.println();

            // Create Webhook
            Map map = new HashMap();
            map.put("url", invokeEndPoint);
            map.put("auth", Constants.OCISIGNATURE);

            Map AuthDetails = new HashMap();
            AuthDetails.put("keyId", Constants.TENANCY_ID + "/" + Constants.USER_ID + "/" + Constants.FINGERPRINT);
            AuthDetails.put("version", "1");

            map.put("authDetails", AuthDetails);

            String jsonValue = JSONValue.toJSONString(map);
            System.out.println(jsonValue);
            entity = new StringEntity(jsonValue, Charset.forName("UTF-8"));

            httpPost = new HttpPost("https://authentication-service-integ-helios-qa2.channels.ocs.oc-test.com/resources/v1/webhooks");
            httpPost.addHeader("Authorization", token);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(entity);
            response = client.execute(httpPost);
            responseJSON = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            System.out.printf(responseJSON);
            jsonObj = (JSONObject) parser.parse(responseJSON);
            webhookId = (String) jsonObj.get("id");
            System.out.printf("\nCreated Webhooks with id %s\n", webhookId);
            System.out.println();
            System.out.println();


            Thread.sleep(2000);

            // Create rule
            String url = String.format("https://authentication-service-integ-helios-qa2.channels.ocs.oc-test.com/resources/v1/webhooks/%s/rules/", webhookId);
            httpPost = new HttpPost(url);

            Map filter = new HashMap();
            filter.put("operator", "none");
            filter.put("children", null);

            Map data = new HashMap();
            data.put("event_id", eventId);
            data.put("filters", filter);

            jsonValue = JSONValue.toJSONString(data);
            entity = new StringEntity(jsonValue, Charset.forName("UTF-8"));
            httpPost.addHeader("Authorization", token);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(entity);

            response = client.execute(httpPost);
            responseJSON = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            System.out.printf(responseJSON);
            jsonObj = (JSONObject) parser.parse(responseJSON);
            String id = (String) jsonObj.get("id");
            System.out.printf("\nCreated rule with id %s\n", id);
            System.out.println();
            System.out.println();
        } catch (IOException | ParseException | InterruptedException e) {
            e.printStackTrace();
        }
        return webhookId;
    }

    // Get the bearer authentication token
    private String getAuthHeader() {
        String auth_header = "";
        try {
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(new HttpGet("https://authentication-service-integ-helios-qa2.channels.ocs.oc-test.com/gen?src=mercury.helios.ui9"));
            String responseJSON = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(responseJSON);
            String token = (String) jsonObj.get("token");
            HttpGet hget = new HttpGet("https://authentication-service-integ-helios-qa2.channels.ocs.oc-test.com/token");
            hget.addHeader("Authorization", "Bearer " + token);
            response = client.execute(hget);
            String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            jsonObj = (JSONObject) parser.parse(responseString);
            return "Bearer " + (String) jsonObj.get("token");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return auth_header;
    }
}
