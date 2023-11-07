package com.ddfplus.bims;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class BimsClient {
    public static final String BIMS_URL  = "https://bims.barchart.com/authenticate";

    private static final Logger logger = LoggerFactory.getLogger(BimsClient.class);
    private  ObjectMapper objectMapper;
    private JsonFactory jsonFactory;

    public BimsClient() {
        this.jsonFactory = new JsonFactory();
        this.objectMapper = new ObjectMapper(jsonFactory);
    }

    public BimsResponse getJwt(String userName, String password,String domain) throws IOException, ParseException {


        try (CloseableHttpClient  client = HttpClients.createDefault()) {
            Map<String,String> post = new HashMap<>();
            post.put("username", userName);
            post.put("password", password);
            post.put("domain", domain);

            HttpEntity httpEntity = HttpEntities.create(objectMapper.writeValueAsBytes(post), ContentType.APPLICATION_JSON);
            ClassicHttpRequest httpPost = ClassicRequestBuilder.post(BIMS_URL)
                    .setEntity(httpEntity)
                    .build();

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                if (response.getCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    BimsResponse rsp = null;
                    if (entity != null) {
                        rsp = objectMapper.readValue(entity.getContent(), BimsResponse.class);
                    }
                    EntityUtils.consumeQuietly(entity);
                    return rsp;
                }
                else {
                    logger.error("Bad Request: code: {} reason: {}",response.getCode(),response.getReasonPhrase());
                }
            }
        }
        return null;
    }
}
