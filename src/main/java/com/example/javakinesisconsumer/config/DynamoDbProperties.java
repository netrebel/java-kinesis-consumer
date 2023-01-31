package com.example.javakinesisconsumer.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.retry.PredefinedRetryPolicies;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for DynamoDB SDK version 1.x.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
@ToString
@Validated
@ConfigurationProperties("amazon.dynamodb")
public class DynamoDbProperties {

    private final Client client = new Client();

    @NotBlank
    private String endpoint;
    @NotBlank
    private String region;

    @NotEmpty
    private final Map<String, TableDefinition> tables = new HashMap<>();

    /**
     * Returns table level information for an item in the {@literal amazon.dynamodb.tables} map in application.yml.
     *
     * @param tableKey the key in the table map
     * @return table definition, including table name. Note, the key is not the table name since each environment has a
     * unique table name.
     */
    public TableDefinition getTableByKey(String tableKey) {
        TableDefinition table = tables.get(tableKey);
        Assert.notNull(table,
              "Table name with the key " + tableKey + " not found. Verify application.yml is correct.");
        return table;
    }

    /**
     * Timeout and retry value configuration:
     *
     * <p>
     * For each {@code @DurationMin} and {@code @DurationMax} annotation below:
     * <ul>
     *     <li>the message is explicitly specified due to bug in the annotations.
     *         The default value does not include the max/min value in the generated message</li>
     *     <li>The duration value is capped at [{@code Integer.MAX_VALUE}]ms because the calling code translates
     *         the value to milliseconds and then truncates it to an int.</li>
     * </ul>
     * </p>
     * The timeout values are related:
     * <p>
     * execution time = retry * (connection timeout + socket timeout)
     * </p>
     * <p>
     * request time = connection timeout + socket timeout
     * </p>
     *
     * @see <a href="https://aws.amazon.com/blogs/database/tuning-aws-java-sdk-http-request-settings-for-latency-aware-amazon-dynamodb-applications/">aws-java-sdk-tuning-for-latency-guide</a>
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    @ToString
    @Validated
    public static class Client {

        /**
         * The amount of time to wait when initially establishing a connection before giving up and timing out. A value of
         * {@literal 0} means infinity, and is not recommended.
         * <p>
         * Default value is {@literal 10} seconds.
         * </p>
         * {@link ClientConfiguration#setConnectionTimeout(int)}
         */
        @DurationUnit(ChronoUnit.SECONDS)
        @DurationMin(seconds = 0, message = "must be greater than or equal to 0 seconds")
        @DurationMax(millis = Integer.MAX_VALUE,
              message = "must be less than or equal to " + Integer.MAX_VALUE + " milliseconds")
        private Duration connectionTimeout =
              Duration.ofMillis(ClientConfiguration.DEFAULT_CONNECTION_TIMEOUT);

        /**
         * The total time spent on all requests across retries. A non-positive value disables this feature.
         * <p>
         * This feature requires buffering the entire response (for non-streaming APIs) into memory to enforce a hard timeout
         * when reading the response. For APIs that return large responses this could be expensive.
         * </p>
         * Disabled by default (value {@literal 0}).
         * <p>
         * {@link ClientConfiguration#setClientExecutionTimeout(int)}
         * </p>
         */
        @DurationUnit(ChronoUnit.SECONDS)
        @DurationMin(seconds = 0, message = "must be greater than or equal to 0 seconds")
        @DurationMax(millis = Integer.MAX_VALUE,
              message = "must be less than or equal to " + Integer.MAX_VALUE + " milliseconds")
        private Duration executionTimeout =
              Duration.ofMillis(ClientConfiguration.DEFAULT_CLIENT_EXECUTION_TIMEOUT);

        /**
         * The timeout for each individual HTTP request (i.e. each retry). The total time to wait for a request to complete
         * before killing it. A non-positive value disables this feature.
         * <p>
         * This feature requires buffering the entire response (for non-streaming APIs) into memory to enforce a hard timeout
         * when reading the response. For APIs that return large responses this could be expensive.
         * </p>
         * Disabled by default (value {@literal 0}).
         * <p>
         * {@link ClientConfiguration#setRequestTimeout(int)}
         * </p>
         */
        @DurationUnit(ChronoUnit.SECONDS)
        @DurationMin(seconds = 0, message = "must be greater than or equal to 0 seconds")
        @DurationMax(millis = Integer.MAX_VALUE,
              message = "must be less than or equal to " + Integer.MAX_VALUE + " milliseconds")
        private Duration requestTimeout = Duration.ofMillis(ClientConfiguration.DEFAULT_REQUEST_TIMEOUT);

        /**
         * The amount of time to wait for data to be transferred over an established, open connection before the connection
         * times out and is closed. A value of {@literal 0} means infinity, and isn't recommended.
         * <p>
         * Default value is {@literal 50} seconds.
         * </p>
         * <p>
         * {@link ClientConfiguration#setSocketTimeout(int)}
         * </p>
         */
        @DurationUnit(ChronoUnit.SECONDS)
        @DurationMin(seconds = 0, message = "must be greater than or equal to 0 seconds")
        @DurationMax(millis = Integer.MAX_VALUE,
              message = "must be less than or equal to " + Integer.MAX_VALUE + " milliseconds")
        private Duration socketTimeout = Duration.ofMillis(ClientConfiguration.DEFAULT_SOCKET_TIMEOUT);

        /**
         * The maximum number of retry attempts for failed requests. Must be either {@code null} or greater than {@literal
         * 0}. When {@code null} or not set, the default retry policy is used for the connection, and not recreated.
         * <p>
         * Default value {@literal 10}.
         * </p>
         * <p>
         * {@link ClientConfiguration#setMaxErrorRetry(int)}
         * </p>
         */
        @Min(1)
        private Integer maxErrorRetries;

        /**
         * When true, a custom retry policy should be created, else, the default one should be used.
         */
        public boolean useCustomRetryPolicy() {
            return maxErrorRetries != null;
        }

        /**
         * Optional is not returned by this method because we want lombok {@code @ToString} to return the actual value, not
         * {@code Optional.empty()}.
         */
        public int getMaxErrorRetries() {
            return (maxErrorRetries == null)
                  ? PredefinedRetryPolicies.DYNAMODB_DEFAULT_MAX_ERROR_RETRY
                  : maxErrorRetries;
        }
    }

    @Setter
    @ToString
    @Validated
    public static class TableDefinition {

        @Getter
        @NotBlank
        private String name;

        @Getter(AccessLevel.PACKAGE)
        private final Map<String, TimeToLive> timeToLive = new HashMap<>();

        /**
         * Returns the time to live for the given {@code key}, as defined in {@literal application.yml} file.
         *
         * @param key defined in {@literal application.yml}
         * @return time to live for the key. A {@code Duration.ZERO} is considered a disabled time to live.
         * @throws IllegalArgumentException the key is not defined in {@literal application.yml}
         */
        public Duration getTimeToLive(String key) {
            Assert.isTrue(isTimeToLiveEnabled(key),
                  String.format("TimeToLive for [%s] is not enabled on TableDefinition %s", key, name));
            return timeToLive.get(key).getValue();
        }

        public boolean isTimeToLiveEnabled(String key) {
            return timeToLive.containsKey(key) && timeToLive.get(key).isTimeToLiveEnabled();
        }
    }

    @ToString
    public static class TimeToLive {

        /**
         * The length of time that an item is considered valid in DynamoDB. After the item expires, DynamoDB deletes it,
         * generally within 48 hours of expiration. Having DynamoDB delete data using a TTL attribute is cheaper than
         * manually doing a write to the db to delete the data. See https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/TTL.html
         * for more information on using TTL.
         * <p/>
         * By default, time to live is set to {@literal 0}, which disables the property.
         *
         * @param value (optional when {@code min} is {@code null}) the time to live value. Time to live is disabled when
         * both {@code value} and {@code min} are null.
         * @param min (optional) minimum duration allowed. Defaults to {@code Duration.ZERO} when null.
         * @param max (optional) maximum duration allowed. {@code value} has no upper bounds when null.
         * @throws IllegalArgumentException a duration is negative, min > max, or the value is out of range
         */
        @ConstructorBinding
        TimeToLive(Duration value, Duration min, Duration max) {
            Assert.isTrue(value == null || !value.isNegative(),
                  "TimeToLive value must be greater than or equal to 0 days");
            Assert.isTrue((min == null || min.compareTo(Duration.ZERO) >= 0),
                  "TimeToLive min must be greater than or equal to 0 days");
            Assert.isTrue((max == null || max.compareTo(Duration.ZERO) >= 0),
                  "TimeToLive max must be greater than or equal to 0 days");

            Duration valueDefaulted = ((value == null) || value.isZero()) ? Duration.ZERO : value;
            Duration minBounded = (min == null) ? Duration.ZERO : min;
            assertValueBounds(valueDefaulted, minBounded, max);

            this.value = valueDefaulted;
        }

        @Getter
        private final Duration value;

        /**
         * Checks if this {@code TimeToLive} object is considered enabled or disabled. A {@code Duration} of {@literal 0} is
         * considered disabled.
         *
         * @return true if the ttl was set and has a positive value
         */
        boolean isTimeToLiveEnabled() {
            return value != null && value != Duration.ZERO;
        }

        /**
         * Asserts that this table's {@code timeToLive} is between the given min and max values, inclusive.
         *
         * @param value the value to check min/max bounds against
         * @param min the minimum duration allowed
         * @param max (optional) the maximum duration allowed. There is no upper bounds when value not set.
         * @throws IllegalArgumentException when the table's configured time to live {@code min} is greater than the {@code
         * max}, or the value is outside the range {@code min} - {@code max}
         */
        private static void assertValueBounds(Duration value, Duration min, Duration max) {

            if ((max != null) && (min.compareTo(max) > 0)) {
                throw new IllegalArgumentException(
                      String.format("'min' value must be less than or equal to 'max' [min=%s, max=%s]", min,
                            max));
            }

            if (min.compareTo(value) > 0) {
                throw new IllegalArgumentException(
                      String.format("'value' must be greater than or equal to the 'min' (value=%s, min=%s)",
                            value, min));
            }

            if ((max != null) && (max.compareTo(value) < 0)) {
                throw new IllegalArgumentException(
                      String.format("'value' must be less than or equal to the 'max' (value=%s, max=%s)",
                            value, max));
            }
        }
    }
}
