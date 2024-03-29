# PCF Config Server Client
SpringBoot app using Spring Cloud Services 3.x (ConfigServer) + CredHub running on PCF.  Spring Cloud Services - Config Server running on PCF allows an application to get configuration data (variables, certs, secrets, etc) dynamically retrieved from a Git repository. Config Server also allows various security backends to be configured (i.e. Vault or CredHub).  In this app we will configure Config Server with CredHub backend to store additional secrets/passwords that will be dynamically given to the application at runtime.  

## Getting started

clone this repository
```
git clone https://github.com/flopez0303/PCFConfigServerClient.git
```

Open a Terminal (e.g., _cmd_ or _bash_ shell)

Change the working directory to be _PCFConfigServerClient_

```
  cd PCFConfigServerClient
```

Open this project in your editor/IDE of choice.


**_STS and Eclipse Import Help_**

Select _File > Import…_. In the susequent dialog choose _Maven > Existing Maven Project_ then click the _Next_ button. In the _Import Maven Project_ dialog browse to the _PCFConfigServerClient_ directory then click the _Open_ button, then click the _Finish_ button.

## Review Rest Controller with ConfigServer

Within your editor/IDE, review the _MessageController_ class:

_com.example.spring.configserver.client.demo.PCFConfigServerClient.controller_ underneath _src/main/java_

```
@RefreshScope
@RestController
public class MessageController {

    // @Value is used to declare/retrieve the attribute from ConfigServer.  Here we are looking for an entry named "message".
    // If "message" is not found, then the text "DefaultMessageWhenConfigServerIsNotFound" will be displayed
    @Value("${message:DefaultMessageWhenConfigServerIsNotFound}")
    private String configServerMessage;

    // @Value is used to declare/retrieve the attribute from ConfigServer also when backed by CredHub.  Here we are looking for an entry
    // named "mycredhubsecret".  If "mycredhubsecret" is not found, then the text "NoCredHubSecretKeyFound" will be displayed
    @Value("${mycredhubsecret:NoCredHubSecretKeyFound}")
    private String credHubSecret;

    public MessageController() {
    }


    @GetMapping("/message")
    public String message() {

        // Write out the Message we obtained from the ConfigServer
        String message = "Found message in Config Server ---> " + configServerMessage + "\n\n <br/><br/><br/><br/>";
        // Write out the Secret we obtained from the CredHub Server
        message += "Found secret in CredHub Server ---> " + credHubSecret;
        return message;
    }
}

```

When the app requests a configuration from the Config Server, it will use a path containing the application name. You can declare the application name in the _application.yml_ file.

```
Spring:
  application:
    name: pcfconfigserverclient
```

Config Server will be looking for a file called _pcfconfigserverclient.yml_ in the Git repository.

The annotation _@RefreshScope_ will allow for refreshing of Spring beans which use configuration served by the Config Server.

The annotation _@Value_ is used for retrieving the configuration data from Config Server and also any secrets stored into CredHub.

To read further documentation about Config Server within PCF, check out the following link [Spring Cloud Services v3.0] (https://docs.pivotal.io/spring-cloud-services/3-0/common/config-server/writing-client-applications.html)



## Build the _PCFConfigServerClient_ application

Return to the Terminal session you opened previously and make sure your working directory is set to be _PCFConfigServerClient_

First we'll run tests
```
  mvn test
```
Next we'll package the application as an executable jar
```
  mvn package
```

## Run the _PCFConfigServerClient_ application

Now we're ready to run the application

Run the application with
```
  mvn spring-boot:run
```

You should see the application start up an embedded Apache Tomcat server on port 8080 (review terminal output):

```
2019-10-17 11:14:20.696  INFO 86953 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2019-10-17 11:14:20.699  INFO 86953 --- [           main] c.c.d.P.PcfConfigServerClientApplication : Started PcfConfigServerClientApplication in 2.303 seconds (JVM running for 2.629)

```

Browse to http://localhost:8080/message  We should see the default messages from our code since we have not yet configured the Config Server or added any secrets into CredHub.

![Image description](images/scs-image1.jpeg)

Stop the _PCFConfigServerClient_ application. In the terminal window type *Ctrl + C*


## Create Spring Cloud Config Server instance in PCF

Now that our application is ready to read its config from a Cloud Config server, we need to deploy one!  This can be done through Cloud Foundry using the services Marketplace.  Browse to the Marketplace in Pivotal Cloud Foundry Apps Manager, navigate to the Space you have been using to push your app, and select Config Server:

![Image description](images/scs-image2.jpg)

In the resulting details page, select the _trial_, single tenant plan.  Name the instance *p-config-server*, select the Space that you've been using to push all your applications.  At this time you don't need to select an application to bind to the service:

![Image description](images/scs-image3.jpeg)

After we create the service instance you'll be redirected to your _Space_ landing page that lists your apps and services.  The config server is deployed on-demand and will take a few moments to deploy.  

We now need to update the service instance with our GIT repository information.

Create a file named `config-server.json` and update its contents to point to your git repo to be used.

```
{
  "git": {
    "uri": "https://github.com/flopez0303/config-repo"
  }
}
```


Using the Cloud Foundry CLI execute the following update service command:

```
cf update-service config-server -c config-server.json
```

We will now bind our application to our config-server within our Cloud Foundry deployment manifest.  Review these entries to the bottom of */PCFConfigServerClient/manifest.yml*

```
  services:
  - p-config-server
```

Complete:
```
applications:
applications:
- name: pcfconfigserverclient
  random-route: true
  instances: 1
  path: ./target/PCFConfigServerClient-0.0.1-SNAPSHOT.jar
  buildpacks:
  - java_buildpack_offline
  env:
    JAVA_OPTS: -Djava.security.egd=file:///dev/urandom
  services:
  - p-config-server
```

## Deploy and test application

Build the application

```
mvn clean package
```

Push application into Cloud Foundry
```
cf push
```

Test your application by navigating to the /message endpoint of the application.  You should now see a message that is read from the Cloud Config Server!

```
This message came from GitHub and provided by Config Server
```

**What just happened??**

A Spring component within the Spring Cloud Starter Config Client module called a _service connector_ automatically detected that there was a Cloud Config service bound into the application.  The service connector configured the application automatically to connect to the Cloud Config Server and downloaded the configuration and wired it into the application

If you navigate to the Git repo we specified for our configuration, https://github.com/flopez0303/config-repo, you'll see a file named _pcfconfigserverclient.yml_.  This filename is the same as our _spring.application.name_ value for our Boot application.  The configuration is read from this file

## Create secrets in CredHub for access within the app via Cloud Config

Now we will go ahead and populate some secrets into CredHub and have our application read them using the Config Server api.

```
cf config-server-add-credhub-secret p-config-server pcfconfigserverclient/default/master/mysecret '{"mycredhubsecret":"secretpassword12345"}'
```
