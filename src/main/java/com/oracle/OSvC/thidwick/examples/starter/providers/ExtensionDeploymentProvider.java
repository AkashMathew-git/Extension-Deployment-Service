package com.oracle.OSvC.thidwick.examples.starter.providers;

import com.codahale.metrics.MetricRegistry;
import com.oracle.OSvC.thidwick.core.configuration.ConfigurationProvider;
import com.oracle.OSvC.thidwick.core.providers.ProviderBase;
import com.oracle.OSvC.thidwick.examples.starter.helper.ExtensionDeployment;
import com.oracle.OSvC.thidwick.examples.starter.helper.WebhookRegister;
import com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyDeploy;
import com.oracle.OSvC.thidwick.examples.starter.model.RequestBodyWebhookReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExtensionDeploymentProvider extends ProviderBase {
    private static Logger logger = LoggerFactory.getLogger(ExtensionDeploymentProvider.class);
    private final ConfigurationProvider configurationProvider;

    @Autowired
    public ExtensionDeploymentProvider(MetricRegistry metricRegistry, ConfigurationProvider configurationProvider) {
        super(metricRegistry);
        this.configurationProvider = configurationProvider;
    }



    public String deployCode(RequestBodyDeploy request) {
        ExtensionDeployment extensionDeployment = new ExtensionDeployment(request);
        return  extensionDeployment.deploy();
//        ExtensionDeploymentCommand command = new ExtensionDeploymentCommand(request, metricRegistry);
//        return command.execute();
    }


    public String registerWebhooks(RequestBodyWebhookReg request)
    {
        WebhookRegister webhookRegister = new WebhookRegister(request);
        return  webhookRegister.registerWebhooks();
    }
}
