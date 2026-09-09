package com.movilidad.backendtelemetria.infrastructure.adapter.output.redis;

import com.movilidad.backendtelemetria.application.port.output.TelemetryCachePort;
import com.movilidad.backendtelemetria.domain.model.LatestVehicleTelemetry;
import com.movilidad.backendtelemetria.domain.model.Telemetry;
import com.movilidad.backendtelemetria.domain.model.VehicleStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class TelemetryRedisAdapter implements TelemetryCachePort {

    private static final Duration DUPLICATE_TTL = Duration.ofSeconds(10);
    private static final Duration LATEST_TTL = Duration.ofMinutes(5);

    private static final String DUPLICATE_KEY_PREFIX =
            "telemetry:duplicate:";

    private static final String LATEST_KEY_PREFIX =
            "telemetry:latest:";

    private static final String STOP_START_KEY_PREFIX =
            "telemetry:stop-start:";

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
    public void saveLatest(Telemetry telemetry, VehicleStatus status) {

        String key = LATEST_KEY_PREFIX + telemetry.getVehicleId();

        TelemetryRedisValue redisValue = new TelemetryRedisValue(
                telemetry.getVehicleId(),
                telemetry.getLatitude(),
                telemetry.getLongitude(),
                telemetry.getTimestamp(),
                status
        );

        try {
            String value = objectMapper.writeValueAsString(redisValue);

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

    @Override
    public Optional<LatestVehicleTelemetry> findLatest(
            String vehicleId
    ) {
        String key = LATEST_KEY_PREFIX + vehicleId;

        String value = redisTemplate
                .opsForValue()
                .get(key);

        if (value == null) {
            return Optional.empty();
        }

        try {
            TelemetryRedisValue redisValue =
                    objectMapper.readValue(
                            value,
                            TelemetryRedisValue.class
                    );

            return Optional.of(
                    new LatestVehicleTelemetry(
                            redisValue.vehicleId(),
                            redisValue.latitude(),
                            redisValue.longitude(),
                            redisValue.timestamp(),
                            redisValue.status()
                    )
            );

        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Unable to deserialize telemetry from Redis",
                    exception
            );
        }
    }

    @Override
    public Optional<Instant> findStopStart(String vehicleId) {
        String key = STOP_START_KEY_PREFIX + vehicleId;

        String value = redisTemplate
                .opsForValue()
                .get(key);

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(Instant.parse(value));
    }

    @Override
    public void saveStopStart(String vehicleId, Instant timestamp) {
        String key = STOP_START_KEY_PREFIX + vehicleId;

        redisTemplate
                .opsForValue()
                .set(
                        key,
                        timestamp.toString(),
                        LATEST_TTL
                );
    }

    @Override
    public void deleteStopStart(String vehicleId) {
        String key = STOP_START_KEY_PREFIX + vehicleId;

        redisTemplate.delete(key);
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
