package com.helix.gpo.web_crm.storage;

import java.time.Duration;

public interface StorageApi {

    // key convention: {consumer}/{id}/{filename} - example: "invoices/{invoiceId}/document.pdf"
    void upload(String key, byte[] content, String contentType);

    String presignedUrl(String key, Duration validFor);

    void delete(String key);

}
