package ru.yandex.cloud.locker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author mzinal
 */
public class LockerClient implements PessimisticLocker {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LockerClient.class);

    private final String urlBase;
    private final CloseableHttpClient client;

    public LockerClient(String urlBase) {
        this.urlBase = urlBase;
        this.client = HttpClients.createDefault();
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch(Exception ex) {
            LOG.warn("Exception thrown on HTTP client closing - ignored", ex);
        }
    }

    @Override
    public LockerResponse lock(LockerRequest request) {
        byte[] bytes = request.toJson().getBytes(StandardCharsets.UTF_8);
        HttpPost post = new HttpPost(urlBase + "/lock");
        post.setEntity(new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length, ContentType.APPLICATION_JSON));
        final LockerResponse response = new LockerResponse();
        try {
            client.execute(post, htr -> {
                int statusCode = htr.getCode();
                if (statusCode != 200) {
                    throw new IOException("Lock failed, status " + String.valueOf(statusCode)
                            + ": " + htr.getReasonPhrase());
                }
                final String text = new BufferedReader(
                    new InputStreamReader(htr.getEntity().getContent(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                EntityUtils.consume(htr.getEntity());
                JSONObject json = new JSONObject(text);
                if (json.get("locked") != null) {
                    JSONArray locked = json.getJSONArray("locked");
                    for (int i=0; i<locked.length(); ++i) {
                        response.getLocked().add(locked.getString(i));
                    }
                }
                if (json.get("remaining") != null) {
                    JSONArray remaining = json.getJSONArray("remaining");
                    for (int i=0; i<remaining.length(); ++i) {
                        response.getRemaining().add(remaining.getString(i));
                    }
                }
                return null;
            });
        } catch(IOException ix) {
            throw new RuntimeException(ix);
        }
        return response;
    }

    @Override
    public void unlock(LockerOwner owner) {
        byte[] bytes = owner.toJson().getBytes(StandardCharsets.UTF_8);
        HttpPost post = new HttpPost(urlBase + "/unlock");
        post.setEntity(new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length, ContentType.APPLICATION_JSON));
        final LockerResponse response = new LockerResponse();
        try {
            client.execute(post, htr -> {
                int statusCode = htr.getCode();
                if (statusCode != 200) {
                    throw new IOException("Unlock failed, status " + String.valueOf(statusCode)
                            + ": " + htr.getReasonPhrase());
                }
                EntityUtils.consume(htr.getEntity());
                return null;
            });
        } catch(IOException ix) {
            throw new RuntimeException(ix);
        }
    }

}
