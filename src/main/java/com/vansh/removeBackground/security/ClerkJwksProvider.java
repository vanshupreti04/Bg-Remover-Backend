package com.vansh.removeBackground.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

@Component
public class ClerkJwksProvider {

    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    private final Map<String, PublicKey> keyCache = new HashMap<>();
    private long lastFetchTime = 0;
    private static final long CACHE_TTL = 360000;

    public PublicKey getPublicKey(String kid) throws Exception {

        if (keyCache.containsKey(kid) && System.currentTimeMillis() - lastFetchTime < CACHE_TTL) {
            return keyCache.get(kid);
        }
        refreshKeys();
        return keyCache.get(kid);
    }

    private void refreshKeys() throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(jwksUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Failed to fetch JWKS. HTTP " + responseCode);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jwks = mapper.readTree(connection.getInputStream());
            JsonNode keys = jwks.get("keys");

            keyCache.clear();

            for (JsonNode keyNode : keys) {
                String kid = keyNode.get("kid").asText();
                String kty = keyNode.get("kty").asText();
                String alg = keyNode.get("alg").asText();

                if ("RSA".equals(kty) && "RS256".equals(alg)) {
                    String n = keyNode.get("n").asText();
                    String e = keyNode.get("e").asText();

                    PublicKey publicKey = createPublicKey(n, e);
                    keyCache.put(kid, publicKey);
                }
            }

            lastFetchTime = System.currentTimeMillis();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception {

        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulus);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponent);

        BigInteger modulusBigInt = new BigInteger(1, modulusBytes);
        BigInteger exponentBigInt = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusBigInt, exponentBigInt);
        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePublic(spec);
    }
}