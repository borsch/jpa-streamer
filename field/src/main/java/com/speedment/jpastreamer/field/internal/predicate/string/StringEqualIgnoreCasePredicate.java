/*
 *
 * Copyright (c) 2006-2020, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.jpastreamer.field.internal.predicate.string;


import com.speedment.jpastreamer.field.predicate.PredicateType;
import com.speedment.jpastreamer.field.trait.HasReferenceValue;

/**
 *
 * @param <ENTITY>  the entity type
 *                *
 * @author  Per Minborg
 * @since   2.2.0
 */
public final class StringEqualIgnoreCasePredicate<ENTITY>
extends AbstractStringPredicate<ENTITY> {

    public StringEqualIgnoreCasePredicate(
            final HasReferenceValue<ENTITY, String> field,
            final String lowerCase) {

        super(PredicateType.EQUAL_IGNORE_CASE, field, lowerCase, entity -> {
            final String fieldValue = field.get(entity);
            return fieldValue != null
                && fieldValue.equalsIgnoreCase(lowerCase);
        });
    }

    @Override
    public StringNotEqualIgnoreCasePredicate<ENTITY> negate() {
        return new StringNotEqualIgnoreCasePredicate<>(getField(), get0());
    }
}