package net.lliira.illyriad.common.net.http;

import org.jsoup.Connection;

import java.util.Optional;

public interface HttpResponseHandler<O> {

    String acceptedContentType();

    Optional<O> produce(Connection.Response response);
}
