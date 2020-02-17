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

package software.amazon.awssdk.extensions.dynamodb.mappingclient.converters.attribute.bundled;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.ItemAttributeValue.fromNumber;
import static software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.ItemAttributeValue.nullValue;
import static software.amazon.awssdk.extensions.dynamodb.mappingclient.converters.attribute.bundled.ConverterTestUtils.transformFrom;
import static software.amazon.awssdk.extensions.dynamodb.mappingclient.converters.attribute.bundled.ConverterTestUtils.transformTo;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.junit.Test;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.bundled.OptionalDoubleAttributeConverter;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.bundled.OptionalIntAttributeConverter;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute.bundled.OptionalLongAttributeConverter;

public class OptionalAttributeConvertersTest {

    @Test
    public void optionalDoubleConverterWorksCorrectly() {
        OptionalDoubleAttributeConverter converter = OptionalDoubleAttributeConverter.create();

        assertThat(transformFrom(converter, OptionalDouble.empty())).isEqualTo(nullValue().toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalDouble.of(-Double.MAX_VALUE))).isEqualTo(fromNumber("-1.7976931348623157E308").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalDouble.of(-Double.MIN_VALUE))).isEqualTo(fromNumber("-4.9E-324").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalDouble.of(0.0))).isEqualTo(fromNumber("0.0").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalDouble.of(Double.MIN_VALUE))).isEqualTo(fromNumber("4.9E-324").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalDouble.of(Double.MAX_VALUE))).isEqualTo(fromNumber("1.7976931348623157E308").toGeneratedAttributeValue());

        assertThat(transformTo(converter, nullValue().toGeneratedAttributeValue())).isEmpty();
        assertThat(transformTo(converter, fromNumber("-1.7976931348623157E308"))).hasValue(-Double.MAX_VALUE);
        assertThat(transformTo(converter, fromNumber("-4.9E-324"))).hasValue(-Double.MIN_VALUE);
        assertThat(transformTo(converter, fromNumber("0.0"))).hasValue(0.0);
        assertThat(transformTo(converter, fromNumber("4.9E-324"))).hasValue(Double.MIN_VALUE);
        assertThat(transformTo(converter, fromNumber("1.7976931348623157E308"))).hasValue(Double.MAX_VALUE);
    }

    @Test
    public void optionalIntConverterWorksCorrectly() {
        OptionalIntAttributeConverter converter = OptionalIntAttributeConverter.create();

        assertThat(transformFrom(converter, OptionalInt.empty())).isEqualTo(nullValue().toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalInt.of(Integer.MIN_VALUE))).isEqualTo(fromNumber("-2147483648").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalInt.of(0))).isEqualTo(fromNumber("0").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalInt.of(Integer.MAX_VALUE))).isEqualTo(fromNumber("2147483647").toGeneratedAttributeValue());

        assertThat(transformTo(converter, nullValue().toGeneratedAttributeValue())).isEmpty();
        assertThat(transformTo(converter, fromNumber("-2147483648"))).hasValue(Integer.MIN_VALUE);
        assertThat(transformTo(converter, fromNumber("0"))).hasValue(0);
        assertThat(transformTo(converter, fromNumber("2147483647"))).hasValue(Integer.MAX_VALUE);
    }

    @Test
    public void optionalLongConverterWorksCorrectly() {
        OptionalLongAttributeConverter converter = OptionalLongAttributeConverter.create();

        assertThat(transformFrom(converter, OptionalLong.empty())).isEqualTo(nullValue().toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalLong.of(Long.MIN_VALUE))).isEqualTo(fromNumber("-9223372036854775808").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalLong.of(0))).isEqualTo(fromNumber("0").toGeneratedAttributeValue());
        assertThat(transformFrom(converter, OptionalLong.of(Long.MAX_VALUE))).isEqualTo(fromNumber("9223372036854775807").toGeneratedAttributeValue());

        assertThat(transformTo(converter, nullValue().toGeneratedAttributeValue())).isEmpty();
        assertThat(transformTo(converter, fromNumber("-9223372036854775808"))).hasValue(Long.MIN_VALUE);
        assertThat(transformTo(converter, fromNumber("0"))).hasValue(0);
        assertThat(transformTo(converter, fromNumber("9223372036854775807"))).hasValue(Long.MAX_VALUE);
    }
}
