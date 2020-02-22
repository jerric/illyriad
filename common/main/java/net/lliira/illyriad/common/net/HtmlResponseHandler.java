package net.lliira.illyriad.common.net;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Optional;

public class HtmlResponseHandler implements HttpResponseHandler<Document> {
    private static final String CONTENT_TYPE = "text/html";

    @Override
    public String acceptedContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public Optional<Document> produce(Connection.Response response) {
        if (response.contentType().contains(CONTENT_TYPE)) {
            try {
                return Optional.ofNullable(response.parse());
            } catch (IOException e) {
                // Do nothing, will fail through and return empty() instead.
            }
        }
        return Optional.empty();
    }
}
