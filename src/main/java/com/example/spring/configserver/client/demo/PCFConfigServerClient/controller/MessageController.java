package com.example.spring.configserver.client.demo.PCFConfigServerClient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RefreshScope
@RestController
public class MessageController {


    @Value("${message:DefaultMessageWhenConfigServerIsNotFound}")
    private String configServerMessage;


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