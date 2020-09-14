package com.oracle.OSvC.thidwick.examples.starter.endpoints;

import com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyDeploy;
import com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyWebhookReg;
import com.oracle.OSvC.thidwick.examples.starter.providers.ExtensionDeploymentProvider;
import com.oracle.OSvC.thidwick.service.base.CORS.annotations.CORS;
import io.swagger.annotations.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Api
@CORS
@Component
public class ExtensionDeploymentEndPoint {
    private final ExtensionDeploymentProvider extensionDeploymentProvider;

    @Autowired
    public ExtensionDeploymentEndPoint(ExtensionDeploymentProvider extensionDeploymentProvider) {
        this.extensionDeploymentProvider = extensionDeploymentProvider;
    }

    @POST
    @Path("deploycode")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Object", value = "Object to be created", required = true, dataType = "com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyDeploy", paramType = "body")
    })
    @ApiOperation(value = "Deploy function", response = String.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = String.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class)})
    public String deployFunction(String requestBody) {
        RequestBodyDeploy request = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject fnInspectObject = (JSONObject) parser.parse(requestBody);
            request = new RequestBodyDeploy((String) fnInspectObject.get("appName"), (String)fnInspectObject.get("functionName"), (String)fnInspectObject.get("imageName"), (String)fnInspectObject.get("imageTag"));
        }
        catch (Exception e) {
            return "";
        }
        return extensionDeploymentProvider.deployCode(request);
    }


    @POST
    @Path("registerwebhook")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Object", value = "Object to be created", required = true, dataType = "com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyWebhookReg", paramType = "body")
    })
    @ApiOperation(value = "Register Webhooks", response = String.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = String.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = String.class)})
    public String registerWebhooks(String requestBody) {
        RequestBodyWebhookReg request = null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject fnInspectObject = (JSONObject) parser.parse(requestBody);
            request = new RequestBodyWebhookReg((String) fnInspectObject.get("invocationEndpoint"), (String)fnInspectObject.get("eventId"));
        }
        catch (Exception e) {
            return "";
        }
        return extensionDeploymentProvider.registerWebhooks(request);
    }

}
