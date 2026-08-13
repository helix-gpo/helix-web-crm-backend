package com.helix.gpo.web_crm.storage;

import java.time.Duration;

public interface StorageApi {

    /**
     * Lädt Bytes unter dem gegebenen Key hoch (überschreibt, falls vorhanden).
     * Key-Konvention: "{verbraucher}/{id}/{dateiname}", z.B. "invoices/{invoiceId}/document.pdf"
     */
    void upload(String key, byte[] content, String contentType);

    /**
     * Erzeugt eine zeitlich befristete, direkt aufrufbare URL - Bucket bleibt privat.
     */
    String presignedUrl(String key, Duration validFor);

    void delete(String key);

}
