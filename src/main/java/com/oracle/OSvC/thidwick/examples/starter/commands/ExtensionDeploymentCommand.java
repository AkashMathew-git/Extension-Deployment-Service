package com.oracle.OSvC.thidwick.examples.starter.commands;
import com.oracle.OSvC.thidwick.examples.starter.helper.ExtensionDeployment;
import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyDeploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionDeploymentCommand  extends HystrixCommand<String> {
    private static Logger logger = LoggerFactory.getLogger(ExtensionDeploymentCommand.class);

    // Metric Names
    private static String executionMeterName = MetricRegistry.name(ExtensionDeploymentCommand.class, "execution", "meter");
    private static String failMeterName = MetricRegistry.name(ExtensionDeploymentCommand.class, "fail.meter");

    // Command Fields
    private RequestBodyDeploy request;
    private MetricRegistry metricRegistry;
    private ExtensionDeployment extensionDeployment;

    public ExtensionDeploymentCommand(RequestBodyDeploy request, MetricRegistry metricRegistry) {
        super(HystrixCommandGroupKey.Factory.asKey("ExtensionDeploymentGroup"));
        this.request = request;
        this.extensionDeployment = new ExtensionDeployment(request);
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected String run() {
        logger.debug("Command Executing");
        metricRegistry.meter(executionMeterName).mark();
        return extensionDeployment.deploy();
    }

    @Override
    protected String getFallback() {
        metricRegistry.meter(failMeterName).mark();
        return "This is the fallback";
    }
}
