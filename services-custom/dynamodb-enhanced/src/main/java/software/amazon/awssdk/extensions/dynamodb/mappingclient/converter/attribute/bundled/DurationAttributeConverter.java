/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.bundled;

import static software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.internal.ConverterUtils.padLeft;

import java.time.Duration;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.AttributeConverter;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.ItemAttributeValue;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.internal.ConverterUtils;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.TypeConvertingVisitor;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.TypeToken;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * A converter between {@link Duration} and {@link AttributeValue}.
 *
 * <p>
 * This stores and reads values in DynamoDB as a number, so that they can be sorted numerically as part of a sort key.
 *
 * <p>
 * Durations are stored in the format "[-]X[.YYYYYYYYY]", where X is the number of seconds in the duration, and Y is the number of
 * nanoseconds in the duration, left padded with zeroes to a length of 9. The Y and decimal point may be excluded for durations
 * that are of whole seconds. The duration may be preceded by a - to indicate a negative duration.
 *
 * <p>
 * Examples:
 * <ul>
 *     <li>{@code Duration.ofDays(1)} is stored as {@code ItemAttributeValueMapper.fromNumber("86400")}</li>
 *     <li>{@code Duration.ofSeconds(9)} is stored as {@code ItemAttributeValueMapper.fromNumber("9")}</li>
 *     <li>{@code Duration.ofSeconds(-9)} is stored as {@code ItemAttributeValueMapper.fromNumber("-9")}</li>
 *     <li>{@code Duration.ofNanos(1_234_567_890)} is stored as {@code ItemAttributeValueMapper.fromNumber("1.234567890")}</li>
 *     <li>{@code Duration.ofMillis(1)} is stored as {@code ItemAttributeValueMapper.fromNumber("0.001000000")}</li>
 *     <li>{@code Duration.ofNanos(1)} is stored as {@code ItemAttributeValueMapper.fromNumber("0.000000001")}</li>
 *     <li>{@code Duration.ofNanos(-1)} is stored as {@code ItemAttributeValueMapper.fromNumber("-0.000000001")}</li>
 * </ul>
 *
 * <p>
 * This can be created via {@link #create()}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class DurationAttributeConverter implements AttributeConverter<Duration> {
    private static final Visitor VISITOR = new Visitor();

    private DurationAttributeConverter() {
    }

    public static DurationAttributeConverter create() {
        return new DurationAttributeConverter();
    }

    @Override
    public TypeToken<Duration> type() {
        return TypeToken.of(Duration.class);
    }

    @Override
    public AttributeValue transformFrom(Duration input) {
        return ItemAttributeValue.fromNumber(input.getSeconds() +
                                             (input.getNano() == 0 ? "" : "." + padLeft(9, input.getNano())))
                                 .toGeneratedAttributeValue();
    }

    @Override
    public Duration transformTo(AttributeValue input) {
        if (input.n() != null) {
            return ItemAttributeValue.fromNumber(input.n()).convert(VISITOR);
        }

        return ItemAttributeValue.fromGeneratedAttributeValue(input).convert(VISITOR);
    }

    private static final class Visitor extends TypeConvertingVisitor<Duration> {
        private Visitor() {
            super(Duration.class, DurationAttributeConverter.class);
        }

        @Override
        public Duration convertNumber(String value) {
            String[] splitOnDecimal = ConverterUtils.splitNumberOnDecimal(value);

            long seconds = Long.parseLong(splitOnDecimal[0]);
            int nanoAdjustment = Integer.parseInt(splitOnDecimal[1]);

            if (seconds < 0) {
                nanoAdjustment = -nanoAdjustment;
            }

            return Duration.ofSeconds(seconds, nanoAdjustment);
        }
    }
}
