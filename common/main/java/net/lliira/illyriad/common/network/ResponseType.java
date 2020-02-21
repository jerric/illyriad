package net.lliira.illyriad.common.network;

public enum ResponseType {
    Html("text/html"),
    Json("application/json");

    private final String contentType;

    ResponseType(final String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
