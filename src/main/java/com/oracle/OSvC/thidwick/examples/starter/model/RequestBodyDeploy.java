package com.oracle.OSvC.thidwick.examples.starter.model;


public class RequestBodyDeploy {
    private String appName;
    private String functionName;
    private String imageName;
    private String imageTag;


    public RequestBodyDeploy(String appName, String functionName, String imageName, String imageTag) {
        this.appName = appName;
        this.functionName = functionName;
        this.imageName = imageName;
        this.imageTag = imageTag;
    }

    public String getAppName() {
        return appName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getImageName() { return imageName; }

    public String getImageTag() { return imageTag; }


}