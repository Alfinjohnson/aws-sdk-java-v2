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

import java.math.BigDecimal;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.AttributeConverter;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.ItemAttributeValue;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.string.bundled.BigDecimalStringConverter;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.TypeConvertingVisitor;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.TypeToken;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * A converter between {@link BigDecimal} and {@link AttributeValue}.
 *
 * <p>
 * This stores values in DynamoDB as a number.
 *
 * <p>
 * This supports perfect precision with the full range of numbers that can be stored in DynamoDB. For less precision or
 * smaller values, consider using {@link FloatAttributeConverter} or {@link DoubleAttributeConverter}.
 *
 * <p>
 * If values are known to be whole numbers, it is recommended to use a perfect-precision whole number representation like those
 * provided by {@link ShortAttributeConverter}, {@link IntegerAttributeConverter} or {@link BigIntegerAttributeConverter}.
 *
 * <p>
 * This can be created via {@link #create()}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class BigDecimalAttributeConverter implements AttributeConverter<BigDecimal> {
    private static final Visitor VISITOR = new Visitor();
    private static final BigDecimalStringConverter STRING_CONVERTER = BigDecimalStringConverter.create();

    private BigDecimalAttributeConverter() {
    }

    public static BigDecimalAttributeConverter create() {
        return new BigDecimalAttributeConverter();
    }

    @Override
    public TypeToken<BigDecimal> type() {
        return TypeToken.of(BigDecimal.class);
    }

    @Override
    public AttributeValue transformFrom(BigDecimal input) {
        return ItemAttributeValue.fromNumber(STRING_CONVERTER.toString(input)).toGeneratedAttributeValue();
    }

    @Override
    public BigDecimal transformTo(AttributeValue input) {
        if (input.n() != null) {
            return ItemAttributeValue.fromNumber(input.n()).convert(VISITOR);
        }
        return ItemAttributeValue.fromGeneratedAttributeValue(input).convert(VISITOR);
    }

    private static final class Visitor extends TypeConvertingVisitor<BigDecimal> {
        private Visitor() {
            super(BigDecimal.class, BigDecimalAttributeConverter.class);
        }

        @Override
        public BigDecimal convertString(String value) {
            return STRING_CONVERTER.fromString(value);
        }

        @Override
        public BigDecimal convertNumber(String value) {
            return STRING_CONVERTER.fromString(value);
        }
    }
}
