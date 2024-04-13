package com.example.myjavafxapp;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSSender {
    public static final String ACCOUNT_SID = "ACf40063e38dd4a198b6079e7f76ea7c21";
    public static final String AUTH_TOKEN = "c9d8b9d9f5b2ef00cc30e9b3a83273b0";

    public void sendMessage(){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message.creator(
                        new PhoneNumber("+13343790412"),
                        new PhoneNumber("+18443430395"),
                        "Hello, this is a test message!")
                .create();
        System.out.println("Message SID: " + message.getSid());
    }
}