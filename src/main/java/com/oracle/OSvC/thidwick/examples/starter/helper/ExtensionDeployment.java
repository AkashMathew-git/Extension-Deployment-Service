package com.oracle.OSvC.thidwick.examples.starter.helper;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyDeploy;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.tomitribe.auth.signatures.PEM;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.oracle.OSvC.thidwick.examples.starter.constants.Constants;

public class ExtensionDeployment {

    long start;
    RequestBodyDeploy request;

    public ExtensionDeployment(RequestBodyDeploy request) {
        this.request = request;
    }

    public String deploy() {
        start = System.currentTimeMillis();
        String appName = request.getAppName();
        String fnName = request.getFunctionName();
        boolean isFnUpdate = false;

        System.out.println();
        System.out.println("********** Extension Deployment Service Starting **********");
        System.out.println();

        String invokeEndPoint = createOrUpdateFunction(appName, fnName, request.getImageName(), request.getImageTag());

        long finish = System.currentTimeMillis();
        System.out.println(String.format("Time Elapsed  %d", finish - start));

        System.out.println();
        System.out.println("********** Extension Deployment Service Finished **********");
        System.out.println();
        return invokeEndPoint;
    }

    /*Creates an application if an application with the given name does not exist,
      Creates a function with given image having given image OCID if the function with given name does not exist,
      Updates the function with given image haing given image OCID if the function with given name already exist,
      Returns the invoke end point of the function created or updated*/
    private String createOrUpdateFunction(String appName, String fnName, String imageName, String imageTag) {
        String invokeEndPoint = "";
        String appId = "";
        String fnId = "";
        boolean isFnUpdate = false;
        try {

            System.out.println(String.format("Create Directory Time Elapsed  %d", System.currentTimeMillis() - start));

            // Check if the app exists and get app details
            // Otherwise create app

            // This is the keyId for a key uploaded through the console
            String apiKey = Constants.TENANCY_ID + "/" + Constants.USER_ID + "/" + Constants.FINGERPRINT;
            PrivateKey privateKey = loadPrivateKey(Constants.PRIVATEKEYPATH);
            RequestSigner signer = new RequestSigner(apiKey, privateKey);

            // Get list of applications in the compartment and check if the application
            // with given appName already exists
            String appList = getApplicationsList(signer, Constants.COMPARTMENT_ID);
            Pattern pattern = Pattern.compile(String.format("\"id\":\"(.*)\",\"displayName\":\"%s\"", appName));
            Matcher matcher = pattern.matcher(appList);
            if (matcher.find()) {
                appId = matcher.group(1);
                System.out.println("Application Id : " + appId);
                System.out.println();
                System.out.println("Getting Functions list");
                String functionsList = getFunctionsList(signer, appId);
                pattern = Pattern.compile(String.format("\"id\":\"([a-zA-Z0-9.]*)\",\"applicationId\":\"%s\",\"displayName\":\"%s\"", appId, fnName));
                matcher = pattern.matcher(functionsList);
                if (matcher.find()) {
                    fnId = matcher.group(1);
                    System.out.println("Function Id : " + fnId);
                    System.out.println();
                    isFnUpdate = true;
                }
            } else {
                // Create application
                String appDetails = createApplication(signer, appName);
                JSONParser parser = new JSONParser();
                JSONObject appDetailObj = (JSONObject) parser.parse(appDetails);
                appId = (String) appDetailObj.get("id");
                System.out.println("Application Id : " + appId);
                System.out.println();
                isFnUpdate = false;
            }
            String functionDetails = null;
            if(imageTag == null || imageTag.isEmpty())
            {
                imageTag = "latest";
            }
            String imageOCID = "phx.ocir.io/cxservice/functions-repo/"+ imageName + ":" + imageTag;
            if (isFnUpdate) {
                // Update the function with given image Id
                functionDetails = updateFunction(signer, fnId, imageOCID);
            } else {
                // Create function with given image id
                 functionDetails = createFunction(signer, appId, fnName, imageOCID, 128);
            }

            if(functionDetails != null) {
                // Get the invocation end point from the function details
                JSONParser parser = new JSONParser();
                JSONObject fnDetailsObj = (JSONObject) parser.parse(functionDetails);
                fnId = (String) fnDetailsObj.get("id");
                invokeEndPoint = (String) fnDetailsObj.get("invokeEndpoint") + "/20181201/functions/" + fnId + "/actions/invoke";
                System.out.println("Invocation End Point : " + invokeEndPoint);
                System.out.println();
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return invokeEndPoint;
    }

    // Loads the private key form the file in the given location
    private PrivateKey loadPrivateKey(String privateKeyFilename) {
        try (InputStream privateKeyStream = Files.newInputStream(Paths.get(privateKeyFilename))) {
            return PEM.readPrivateKey(privateKeyStream);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid format for private key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load private key");
        }
    }

    // Creates a new application with given name using OCI Rest APIs
    private String createApplication(RequestSigner signer, String appName) {

        String appDetails = "";
        try {
            HttpRequestBase request;
            HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor() {
                @Override
                public void process(HttpRequest request, HttpContext context) throws
                        HttpException, IOException {
                    if (request.containsHeader("Content-Length")) {
                        request.removeHeaders("Content-Length");
                    }
                }
            };

            HttpClient client = HttpClients.custom()
                    .addInterceptorFirst(requestInterceptor)
                    .build();
            System.out.println("Creating Application");
            System.out.println();
            String uri = "https://functions.us-phoenix-1.oci.oraclecloud.com/20181201/applications";
            request = new HttpPost(uri);
            HttpEntity entity = new StringEntity("{\n" +
                    String.format("    \"compartmentId\": \"%s\",\n", Constants.COMPARTMENT_ID) +
                    String.format("    \"displayName\": \"%s\",\n", appName) +
                    "    \"subnetIds\": [\n" +
                    String.format("    \"%s\"\n", Constants.SUBNET_ID) +
                    "    ]\n" +
                    "}");
            ((HttpPost) request).setEntity(entity);
            signer.signRequest(request);
            System.out.println("\n Uri : " + uri);
            System.out.println();
            System.out.println("Authorization : " + request.getFirstHeader("Authorization"));
            HttpResponse response = client.execute(request);
            appDetails = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            System.out.println(appDetails);
            System.out.println(String.format("Create App Time Elapsed  %d", System.currentTimeMillis() - start));
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appDetails;
    }

    // Gets the list of applications in the given compartment
    private String getApplicationsList(RequestSigner signer, String compartmentId) throws IOException {
        String appList = "";
        HttpRequestBase request;
        HttpClient client = HttpClients.createDefault();
        System.out.println("Getting Application List");
        System.out.println();
        String uri = String.format("https://functions.us-phoenix-1.oci.oraclecloud.com/20181201/applications?compartmentId=%s", compartmentId);
        request = new HttpGet(uri);
        signer.signRequest(request);
        System.out.println("\n Uri : " + uri);
        System.out.println();
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        appList = EntityUtils.toString(entity, "UTF-8");
        System.out.println(appList);
        return appList;
    }

    // Gets the list of functions in the given application
    private String getFunctionsList(RequestSigner signer, String appId) throws IOException {
        String functionList = "";
        HttpRequestBase request;
        HttpClient client = HttpClients.createDefault();
        System.out.println("Getting Functions List");
        System.out.println();
        String uri = String.format("https://functions.us-phoenix-1.oci.oraclecloud.com/20181201/functions?applicationId=%s", appId);
        request = new HttpGet(uri);
        signer.signRequest(request);
        System.out.println("\n Uri : " + uri);
        System.out.println();
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        functionList = EntityUtils.toString(entity, "UTF-8");
        System.out.println(functionList);
        return functionList;
    }

    // Creates a new function with given name, image OCID, and memory size
    private String createFunction(RequestSigner signer, String appId, String fnName, String imageId, int sizeInMB) {

        String appDetails = "";
        try {
            HttpRequestBase request;
            HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor() {
                @Override
                public void process(HttpRequest request, HttpContext context) throws
                        HttpException, IOException {
                    if (request.containsHeader("Content-Length")) {
                        request.removeHeaders("Content-Length");
                    }
                }
            };

            HttpClient client = HttpClients.custom()
                    .addInterceptorFirst(requestInterceptor)
                    .build();
            System.out.println("Creating Functions");
            System.out.println();
            String uri = "https://functions.us-phoenix-1.oci.oraclecloud.com/20181201/functions";
            request = new HttpPost(uri);
            HttpEntity entity = new StringEntity("{\n" +
                    String.format("    \"applicationId\": \"%s\",\n", appId) +
                    String.format("    \"displayName\": \"%s\",\n", fnName) +
                    String.format("    \"Image\": \"%s\",\n", imageId) +
                    String.format("    \"memoryInMBs\": %d\n", sizeInMB) +
                    "}");
            ((HttpPost) request).setEntity(entity);
            signer.signRequest(request);
            System.out.println("\n Uri : " + uri);
            System.out.println();
            System.out.println("Authorization : " + request.getFirstHeader("Authorization"));
            HttpResponse response = client.execute(request);
            appDetails = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            System.out.println(appDetails);
            System.out.println(String.format("Create Function Time Elapsed  %d", System.currentTimeMillis() - start));
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appDetails;
    }

    // Updates an existing functions image with given image OCID
    private String updateFunction(RequestSigner signer, String fnId, String imageId) throws IOException {
        String functionDetails;
        HttpRequestBase request;
        HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws
                    HttpException, IOException {
                if (request.containsHeader("Content-Length")) {
                    request.removeHeaders("Content-Length");
                }
            }
        };

        HttpClient client = HttpClients.custom()
                .addInterceptorFirst(requestInterceptor)
                .build();
        System.out.println("Updating Function");
        System.out.println();
        String uri = "https://functions.us-phoenix-1.oci.oraclecloud.com/20181201/functions/" + fnId;
        request = new HttpPut(uri);
        HttpEntity entity = new StringEntity("{\n" +
                String.format("    \"Image\": \"%s\"\n", imageId) +
                "}");
        ((HttpPut) request).setEntity(entity);
        signer.signRequest(request);
        System.out.println("\n Uri : " + uri);
        System.out.println();
        System.out.println("Authorization : " + request.getFirstHeader("Authorization"));
        HttpResponse response = client.execute(request);
        functionDetails = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
        System.out.println(functionDetails);
        System.out.println(String.format("Update Function Time Elapsed  %d", System.currentTimeMillis() - start));
        System.out.println();
        return functionDetails;
    }

}

