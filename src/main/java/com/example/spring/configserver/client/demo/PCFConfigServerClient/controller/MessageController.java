package com.example.spring.configserver.client.demo.PCFConfigServerClient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


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