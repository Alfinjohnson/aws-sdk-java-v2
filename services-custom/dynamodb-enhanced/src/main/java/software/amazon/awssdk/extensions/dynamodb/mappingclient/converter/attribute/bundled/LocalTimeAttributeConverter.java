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
import static software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.internal.ConverterUtils.padLeft2;

import java.time.DateTimeException;
import java.time.LocalTime;
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
 * A converter between {@link LocalTime} and {@link AttributeValue}.
 *
 * <p>
 * This stores and reads values in DynamoDB as a number, so that they can be sorted numerically as part of a sort key.
 *
 * <p>
 * LocalTimes are stored in the format "HHIISS[.NNNNNNNNN]", where:
 * <ol>
 *     <li>H is a 2-character, zero-prefixed hour between 00 and 23</li>
 *     <li>I is a 2-character, zero-prefixed minute between 00 and 59</li>
 *     <li>S is a 2-character, zero-prefixed second between 00 and 59</li>
 *     <li>N is a 9-character, zero-prefixed nanosecond between 000,000,000 and 999,999,999.
 *     The . and N may be excluded if N is 0.</li>
 * </ol>
 *
 * <p>
 * Examples:
 * <ul>
 *     <li>{@code LocalTime.of(5, 30, 0)} is stored as {@code ItemAttributeValueMapper.fromNumber("053000")}</li>
 *     <li>{@code LocalDateTime.of(5, 30, 0, 1)} is stored as {@code ItemAttributeValueMapper.fromNumber("053000.000000001")}</li>
 * </ul>
 *
 * <p>
 * This can be created via {@link #create()}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class LocalTimeAttributeConverter implements AttributeConverter<LocalTime> {
    private static final Visitor VISITOR = new Visitor();

    private LocalTimeAttributeConverter() {
    }

    public static LocalTimeAttributeConverter create() {
        return new LocalTimeAttributeConverter();
    }

    @Override
    public TypeToken<LocalTime> type() {
        return TypeToken.of(LocalTime.class);
    }

    @Override
    public AttributeValue transformFrom(LocalTime input) {
        String value = "" +
                       padLeft2(input.getHour()) +
                       padLeft2(input.getMinute()) +
                       padLeft2(input.getSecond()) +
                       (input.getNano() == 0 ? "" : "." + padLeft(9, input.getNano()));
        return ItemAttributeValue.fromNumber(value).toGeneratedAttributeValue();
    }

    @Override
    public LocalTime transformTo(AttributeValue input) {
        if (input.n() != null) {
            return ItemAttributeValue.fromNumber(input.n()).convert(VISITOR);
        }

        return ItemAttributeValue.fromGeneratedAttributeValue(input).convert(VISITOR);
    }

    private static final class Visitor extends TypeConvertingVisitor<LocalTime> {
        private Visitor() {
            super(LocalTime.class, InstantAsIntegerAttributeConverter.class);
        }

        @Override
        public LocalTime convertNumber(String value) {
            String[] splitOnDecimal = ConverterUtils.splitNumberOnDecimal(value);
            String[] chunkedTime = ConverterUtils.chunk(splitOnDecimal[0], 2, 2, 2);

            try {
                return LocalTime.of(Integer.parseInt(chunkedTime[0]),
                                    Integer.parseInt(chunkedTime[1]),
                                    Integer.parseInt(chunkedTime[2]),
                                    Integer.parseInt(splitOnDecimal[1]));
            } catch (DateTimeException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
