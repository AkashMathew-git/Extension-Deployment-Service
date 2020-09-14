package com.oracle.OSvC.thidwick.examples.starter;

import com.oracle.OSvC.thidwick.service.base.ThidwickApplication;
import com.oracle.OSvC.thidwick.service.base.config.ApplicationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

// Tell spring that this class contains configuration information.
@Configuration
// Tell spring where to look for service properties that are defined in the code.
// Determine the appropriate profiles for your application and set a default, such as local here.
@PropertySource({"classpath:application.properties", "classpath:application-${profile:local}.properties"})
// Tell spring what other places contain configuration to pull.
// ApplicationConfig is a class in service-base that configures all the base Beans for the service.
@Import({ApplicationConfig.class})
// Tell spring what package to look for spring components and configuration.
// This should be updated to reflect the actual package name of your application.
@ComponentScan("com.oracle.OSvC.thidwick.examples.starter")
public class Application {

    /**
     * Starts the thidwick application. This call is required, even if this service does not provisde any web endpoints
     * as it starts the requires services to expose healthchecks, metrics, and other items required.
     * @param args Standard application arguments.
     */
    public static void main(String[] args) {
        ThidwickApplication.run(Application.class, args);
    }

//    Register any beans required by your application here
//    @Bean
//    public Foo foo(Bar bar, MetricRegistry metricRegistry) {
//        return new Foo(bar, metricRegistry);
//    }

}
