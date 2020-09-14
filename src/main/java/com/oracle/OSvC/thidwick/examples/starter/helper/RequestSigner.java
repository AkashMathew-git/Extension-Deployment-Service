package com.oracle.OSvC.thidwick.examples.starter.helper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.tomitribe.auth.signatures.MissingRequiredHeaderException;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class RequestSigner {
    private static final SimpleDateFormat DATE_FORMAT;
    private static final String SIGNATURE_ALGORITHM = "rsa-sha256";
    private static final Map<String, List<String>> REQUIRED_HEADERS;

    static {
        DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        REQUIRED_HEADERS = ImmutableMap.<String, List<String>>builder()
                .put("get", ImmutableList.of("date", "(request-target)", "host"))
                .put("head", ImmutableList.of("date", "(request-target)", "host"))
                .put("delete", ImmutableList.of("date", "(request-target)", "host"))
                .put("put", ImmutableList.of("date", "(request-target)", "host", "content-length", "content-type", "x-content-sha256"))
                .put("post", ImmutableList.of("date", "(request-target)", "host", "content-length", "content-type", "x-content-sha256"))
                .build();
    }

    private final Map<String, Signer> signers;

    /**
     * @param apiKey     The identifier for a key uploaded through the console.
     * @param privateKey The private key that matches the uploaded public key for the given apiKey.
     */
    public RequestSigner(String apiKey, Key privateKey) {
        this.signers = REQUIRED_HEADERS
                .entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> buildSigner(apiKey, privateKey, entry.getKey())));
    }

    /**
     * Create a {@link Signer} that expects the headers for a given method.
     *
     * @param apiKey     The identifier for a key uploaded through the console.
     * @param privateKey The private key that matches the uploaded public key for the given apiKey.
     * @param method     HTTP verb for this signer
     * @return
     */
    protected Signer buildSigner(String apiKey, Key privateKey, String method) {
        final Signature signature = new Signature(
                apiKey, SIGNATURE_ALGORITHM, null, REQUIRED_HEADERS.get(method.toLowerCase()));
        return new Signer(privateKey, signature);
    }

    /**
     * Sign a request, optionally including additional headers in the signature.
     *
     * <ol>
     * <li>If missing, insert the Date header (RFC 2822).</li>
     * <li>If PUT or POST, insert any missing content-type, content-length, x-content-sha256</li>
     * <li>Verify that all headers to be signed are present.</li>
     * <li>Set the request's Authorization header to the computed signature.</li>
     * </ol>
     *
     * @param request The request to sign
     */
    public void signRequest(HttpRequestBase request) {
        final String method = request.getMethod().toLowerCase();
        // nothing to sign for options
        if (method.equals("options")) {
            return;
        }

        final String path = extractPath(request.getURI());

        // supply date if missing
        if (!request.containsHeader("date")) {
            request.addHeader("date", DATE_FORMAT.format(new Date()));
        }

        // supply host if mossing
        if (!request.containsHeader("host")) {
            request.addHeader("host", request.getURI().getHost());
        }

        // supply content-type, content-length, and x-content-sha256 if missing (PUT and POST only)
        if (method.equals("put") || method.equals("post")) {
            if (!request.containsHeader("content-type")) {
                request.addHeader("content-type", "application/json");
            }
            if (!request.containsHeader("content-length") || !request.containsHeader("x-content-sha256")) {
                byte[] body = getRequestBody((HttpEntityEnclosingRequestBase) request);
                if (!request.containsHeader("content-length")) {
                    request.addHeader("content-length", Integer.toString(body.length));
                }
                if (!request.containsHeader("x-content-sha256")) {
                    request.addHeader("x-content-sha256", calculateSHA256(body));
                }
            }
        }

        final Map<String, String> headers = extractHeadersToSign(request);
        final String signature = this.calculateSignature(method, path, headers);
        request.setHeader("Authorization", signature);
//            String sign = "Signature version=\"1\",headers=\"date (request-target) host content-length content-type x-content-sha256\",keyId=\"ocid1.tenancy.oc1..aaaaaaaa53f5u5iqz4tpqtzn26324mw3dc3izz5xrad5girusotnxzps2pta/ocid1.user.oc1..aaaaaaaa6atpgmppy7umysems67cpgrn2nab3cpop56tms4v5q3tmjfltphq/fc:a5:3d:52:d0:9f:6f:41:e7:d2:58:e9:70:cf:12:24\",algorithm=\"rsa-sha256\",signature=\"uvEzjCds+KfXcq6KDotVQMAKQ4l0/YeDaQQ/7B1dKj5G2bi7GWxfnjbj2KQM+ZTfcVR6dwReuoi+U+Rpzyxebs9A17c2COAmfBCRbJSaBzEHevpBNoIrlCmQ2gS1tmN15Xn+R4Vn2TBuFD/pcUQmuLZxx/IKBWpr/eela3GDJjsyt2ydC5f9qjmjlPpxir09oWMl2d3xwvsyr0kW3cX/fD70B+HfqOA6UvBgemLn8+X3lKN8xbbAFuDn6lx3j6E4VR0KoPUUspGK6Ux5GUPo6Qs2BGWRAnObWSpfjKCUQsb/CYD3RtFZXB+6oA4J4w+Squg9eoxWmBACaVpex/YjBg==\"";
//            request.setHeader("Authorization", sign);
    }

    /**
     * Extract path and query string to build the (request-target) pseudo-header.
     * For the URI "http://www.host.com/somePath?foo=bar" return "/somePath?foo=bar"
     */
    private static String extractPath(URI uri) {
        String path = uri.getRawPath();
        String query = uri.getRawQuery();
        if (query != null && !query.trim().isEmpty()) {
            path = path + "?" + query;
        }
        return path;
    }

    /**
     * Extract the headers required for signing from a {@link HttpRequestBase}, into a Map
     * that can be passed to {@link RequestSigner#calculateSignature}.
     *
     * <p>
     * Throws if a required header is missing, or if there are multiple values for a single header.
     * </p>
     *
     * @param request The request to extract headers from.
     */
    private static Map<String, String> extractHeadersToSign(HttpRequestBase request) {
        List<String> headersToSign = REQUIRED_HEADERS.get(request.getMethod().toLowerCase());
        if (headersToSign == null) {
            throw new RuntimeException("Don't know how to sign method " + request.getMethod());
        }
        return headersToSign.stream()
                // (request-target) is a pseudo-header
                .filter(header -> !header.toLowerCase().equals("(request-target)"))
                .collect(Collectors.toMap(
                        header -> header,
                        header -> {
                            if (!request.containsHeader(header)) {
                                throw new MissingRequiredHeaderException(header);
                            }
                            if (request.getHeaders(header).length > 1) {
                                throw new RuntimeException(
                                        String.format("Expected one value for header %s", header));
                            }
                            return request.getFirstHeader(header).getValue();
                        }));
    }

    /**
     * Wrapper around {@link Signer}, returns the {@link Signature} as a String.
     *
     * @param method  Request method (GET, POST, ...)
     * @param path    The path + query string for forming the (request-target) pseudo-header
     * @param headers Headers to include in the signature.
     */
    private String calculateSignature(String method, String path, Map<String, String> headers) {
        Signer signer = this.signers.get(method);
        if (signer == null) {
            throw new RuntimeException("Don't know how to sign method " + method);
        }
        try {
            return signer.sign(method, path, headers).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Calculate the Base64-encoded string representing the SHA256 of a request body
     *
     * @param body The request body to hash
     */
    private String calculateSHA256(byte[] body) {
        byte[] hash = Hashing.sha256().hashBytes(body).asBytes();
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Helper to safely extract a request body.  Because an {@link HttpEntity} may not be repeatable,
     * this function ensures the entity is reset after reading.  Null entities are treated as an empty string.
     *
     * @param request A request with a (possibly null) {@link HttpEntity}
     */
    private byte[] getRequestBody(HttpEntityEnclosingRequestBase request) {
        HttpEntity entity = request.getEntity();
        // null body is equivalent to an empty string
        if (entity == null) {
            return "".getBytes(StandardCharsets.UTF_8);
        }
        // May need to replace the request entity after consuming
        boolean consumed = !entity.isRepeatable();
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try {
            entity.writeTo(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy request body", e);
        }
        // Replace the now-consumed body with a copy of the content stream
        byte[] body = content.toByteArray();
        if (consumed) {
            request.setEntity(new ByteArrayEntity(body));
        }
        return body;
    }
}