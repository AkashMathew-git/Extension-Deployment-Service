# Thidwick Starter
Simple hello world service that exposes an endponit to say hello. It does so through a hystrix command to show how hystrix commands can be used within a service.

## Cloning

Command line 
```bash
git clone git@orahub.oraclecorp.com:appdev-cloud-rnpd/thidwick-starter.git
```

## Configuring
By default the service will run with the local profile. With this profile it will run on port 8080. 
Other than that there is very little to configure for this service.

## Building

IDE Run the gradle build command or use the IDE's build.

Command Line
```bash
./gradlew build
```

## Running

Command Line
```bash
./gradlew run
```

## Testing
Once the service is running, open a broswer and navigate to http://localhost:8080/swagger-ui. From here you can see the endponits available and test them out. Once you have made a few calls, browse to http://localhost:8080/metrics. Here you can see some of the metrics for the service as things were called.