package com.caitu99.lsp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AppConfig {

    @Value("${upload.path}")
    public String uploadPath;
    @Value("${mode}")
    private String mode;
    @Value("${bank.envelop.title}")
    private String envelopKeyStr;
    @Value("${parser.check.sender}")
    private boolean checkSender;

    private List<String> envelopKeys;

    public boolean inDevMode() {
        return mode.equals("dev");
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public List<String> getEnvelopKeys() {
        if (envelopKeys == null) {
            envelopKeys = Arrays.asList(envelopKeyStr.split("\\|"));
        }
        return envelopKeys;
    }

    public boolean getCheckSender() {
        return checkSender;
    }

}
