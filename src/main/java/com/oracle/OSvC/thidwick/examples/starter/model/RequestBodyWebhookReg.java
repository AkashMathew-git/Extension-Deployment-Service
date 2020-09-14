package com.oracle.OSvC.thidwick.examples.starter.model;

public class RequestBodyWebhookReg {
    private  String invocationEndpoint;
    private  String eventId;

    public RequestBodyWebhookReg(String invocationEndPoint, String eventId)
    {
        this.invocationEndpoint = invocationEndPoint;
        this.eventId = eventId;
    }

    public String getInvocationEndpoint(){return invocationEndpoint;}

    public String getEventId(){return eventId;}
}
