package com.movilidad.backendtelemetria.infrastructure.adapter.output.redis;

import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Component
public class TelemetryRedisAdapter implements TelemetryCachePort {

    private static final Duration DUPLICATE_TTL = Duration.ofSeconds(10);
    private static final Duration LATEST_TTL = Duration.ofSeconds(60);

    private static final String DUPLICATE_KEY_PREFIX =
            "telemetry:duplicate:";

    private static final String LATEST_KEY_PREFIX =
            "telemetry:latest:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public TelemetryRedisAdapter(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean registerIfNotDuplicate(Telemetry telemetry) {

        String key = DUPLICATE_KEY_PREFIX + buildFingerprint(telemetry);

        Boolean registered = redisTemplate
                .opsForValue()
                .setIfAbsent(key, "1", DUPLICATE_TTL);

        return Boolean.TRUE.equals(registered);
    }

    @Override
    public void saveLatest(Telemetry telemetry) {

        String key = LATEST_KEY_PREFIX + telemetry.getVehicleId();

        try {
            String value = objectMapper.writeValueAsString(telemetry);

            redisTemplate
                    .opsForValue()
                    .set(key, value, LATEST_TTL);

        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Unable to serialize telemetry for Redis",
                    exception
            );
        }
    }

    private String buildFingerprint(Telemetry telemetry) {

        String raw = String.join(
                "|",
                telemetry.getVehicleId(),
                Double.toString(telemetry.getLatitude()),
                Double.toString(telemetry.getLongitude()),
                telemetry.getTimestamp().toString()
        );

        return sha256(raw);
    }

    private String sha256(String value) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte item : hash) {
                result.append(String.format("%02x", item));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }
}
