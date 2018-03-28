package org.patientview.api.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.patientview.api.service.CaptchaService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/12/2014
 */
@Service
public class CaptchaServiceImpl extends AbstractServiceImpl<CaptchaServiceImpl> implements CaptchaService {

    @Inject
    private Properties properties;

    @Override
    public boolean verify(String captcha) {
        String captchaPrivateKey = properties.getProperty("recaptcha.private.key");
        String captchaUrl = properties.getProperty("recaptcha.url");

        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(captchaUrl + "?secret=" + captchaPrivateKey + "&response=" + captcha);
            HttpResponse response = client.execute(get);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            String stringResult = result.toString();
            JSONObject jsonObj = new JSONObject(stringResult);

            return jsonObj.getBoolean("success");
        } catch (IOException e) {
            LOG.error("Cannot verify captcha: ", e);
            return false;
        }
    }
}
