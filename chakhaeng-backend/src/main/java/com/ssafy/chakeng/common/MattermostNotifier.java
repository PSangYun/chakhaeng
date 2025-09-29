package com.ssafy.chakeng.common;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class MattermostNotifier {
    private final String webhookUrl = "https://meeting.ssafy.com/hooks/u9g44idunjbnmgux9ikrewb3ah";

    public void sendNotification(String message) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String payload = "{ \"text\": \"" + message + "\" }";

        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
}

