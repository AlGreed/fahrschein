package org.zalando.fahrschein.http.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class HeadersImpl implements Headers {
    private final Map<String, String> caseMapping;
    private final Map<String, List<String>> headers;
    private final boolean readOnly;

    private HeadersImpl(boolean readOnly) {
        this.caseMapping = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.headers = new LinkedHashMap<>();
        this.readOnly = false;
    }

    public HeadersImpl() {
        this(false);
    }

    public HeadersImpl(Map<String, List<String>> headers) {
        this(true);
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            final String headerName = entry.getKey();
            final List<String> value = entry.getValue();
            if (value.size() > 0) {
                caseMapping.put(headerName, headerName);
                this.headers.put(headerName, value);
            }
        }
    }

    public HeadersImpl(Headers headers, boolean readOnly) {
        this(readOnly);
        for (String headerName : headers.headerNames()) {
            final List<String> value = headers.get(headerName);
            if (value.size() > 0) {
                caseMapping.put(headerName, headerName);
                this.headers.put(headerName, value);
            }
        }
    }

    @Override
    public List<String> get(String headerName) {
        String caseInsensitiveHeaderName = caseMapping.get(headerName);
        if (caseInsensitiveHeaderName == null) {
            caseMapping.put(headerName, headerName);
            caseInsensitiveHeaderName = headerName;
        }

        final List<String> list = headers.get(caseInsensitiveHeaderName);
        if (list == null) {
            if (readOnly) {
                return Collections.emptyList();
            } else {
                List<String> newList = new ArrayList<>();
                headers.put(caseInsensitiveHeaderName, newList);
                return newList;
            }
        } else {
            return readOnly ? Collections.unmodifiableList(list) : list;

        }
    }

    @Override
    public void add(String headerName, String value) {
        get(headerName).add(value);
    }

    @Override
    public void put(String headerName, String value) {
        final List<String> list = get(headerName);
        if (!list.isEmpty()) {
            list.clear();
        }
        list.add(value);
    }

    @Override
    public String getFirst(String headerName) {
        final List<String> list = get(headerName);
        return (list != null && list.size() > 0 ? list.get(0) : null);
    }

    @Override
    public Set<String> headerNames() {
        return Collections.unmodifiableSet(headers.keySet());
    }

    @Override
    public long getContentLength() {
        final String value = getFirst(CONTENT_LENGTH);
        return value == null ? -1 : Long.parseLong(value);
    }

    @Override
    public void setContentLength(long contentLength) {
        put(CONTENT_LENGTH, String.valueOf(contentLength));

    }

    @Override
    public ContentType getContentType() {
        final String value = getFirst(CONTENT_TYPE);
        return value == null ? null : ContentType.valueOf(value);
    }

    @Override
    public void setContentType(ContentType contentType) {
        put(CONTENT_TYPE, contentType.getValue());
    }
}
