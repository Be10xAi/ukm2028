package com.tech10x.ukm.security;

import com.tech10x.ukm.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Image / document URLs are stored as plain strings that the frontend later renders or links to,
 * so an unchecked URL is an XSS ({@code javascript:}), phishing or tracking-pixel vector.
 * Only https URLs on the configured storage host(s) ({@code app.storage.allowed-hosts},
 * comma-separated, {@code *.example.net} wildcards allowed) are accepted.
 * <p>
 * In production set the list to the exact storage account host, e.g.
 * {@code ukmprod.blob.core.windows.net}, rather than the whole blob.core.windows.net domain.
 */
@Component
public class SafeUrlPolicy {

    private final List<String> allowedHosts;

    public SafeUrlPolicy(@Value("${app.storage.allowed-hosts}") String allowedHosts) {
        this.allowedHosts = Arrays.stream(allowedHosts.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public void requireAllowed(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException | NullPointerException e) {
            throw invalid();
        }

        String host = uri.getHost();
        boolean ok = "https".equalsIgnoreCase(uri.getScheme())
                && host != null
                && uri.getUserInfo() == null                       // blocks https://good.host@evil.host
                && (uri.getPort() == -1 || uri.getPort() == 443)
                && uri.getPath() != null && !uri.getPath().contains("..")
                && hostAllowed(host.toLowerCase(Locale.ROOT));
        if (!ok) {
            throw invalid();
        }
    }

    private boolean hostAllowed(String host) {
        for (String allowed : allowedHosts) {
            if (allowed.startsWith("*.")) {
                String suffix = allowed.substring(1); // ".blob.core.windows.net"
                if (host.endsWith(suffix) && host.length() > suffix.length()) {
                    return true;
                }
            } else if (host.equals(allowed)) {
                return true;
            }
        }
        return false;
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.BAD_REQUEST,
                "url must be an https link to the platform's file storage");
    }
}
