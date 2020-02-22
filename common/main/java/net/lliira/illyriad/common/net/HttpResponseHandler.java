package net.lliira.illyriad.common.net;

import org.jsoup.Connection;

import java.util.Optional;

public interface HttpResponseHandler<T> {

    String acceptedContentType();

    Optional<T> produce(Connection.Response response);
}
