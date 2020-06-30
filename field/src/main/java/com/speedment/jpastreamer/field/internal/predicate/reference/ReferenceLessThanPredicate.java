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
package com.speedment.jpastreamer.field.internal.predicate.reference;

import com.speedment.jpastreamer.field.internal.predicate.AbstractFieldPredicate;
import com.speedment.jpastreamer.field.predicate.PredicateType;
import com.speedment.jpastreamer.field.trait.HasArg0;
import com.speedment.jpastreamer.field.trait.HasReferenceValue;

/**
 *
 * @param <ENTITY>  the entity type
 * @param <V>       the value type
 * 
 * @author  Per Minborg
 * @since   2.2.0
 */
public final class ReferenceLessThanPredicate<ENTITY, V extends Comparable<? super V>>
extends AbstractFieldPredicate<ENTITY,
        HasReferenceValue<ENTITY, V>>
implements HasArg0<V> {
    
    private final V value;

    public ReferenceLessThanPredicate(HasReferenceValue<ENTITY, V> field, V value) {
        super(PredicateType.LESS_THAN, field, entity -> {
            final V fieldValue = field.get(entity);
            if (fieldValue == null || value == null) {
                return false;
            } else return fieldValue.compareTo(value) < 0;
        });
        
        this.value = value;
    }

    @Override
    public V get0() {
        return value;
    }

    @Override
    public ReferenceGreaterOrEqualPredicate<ENTITY, V> negate() {
        return new ReferenceGreaterOrEqualPredicate<>(getField(), value);
    }
    
}