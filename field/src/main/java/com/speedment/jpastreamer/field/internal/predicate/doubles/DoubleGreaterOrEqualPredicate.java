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
package com.speedment.jpastreamer.field.internal.predicate.doubles;

import com.speedment.jpastreamer.field.internal.predicate.AbstractFieldPredicate;
import com.speedment.jpastreamer.field.trait.HasArg0;
import com.speedment.jpastreamer.field.trait.HasDoubleValue;
import com.speedment.jpastreamer.field.predicate.PredicateType;

/**
 * A predicate that evaluates if a value is {@code >=} a specified {@code
 * double}.
 * 
 * @param <ENTITY> entity type
 *
 * @author Emil Forslund
 * @since  3.0.0
 */
public final class DoubleGreaterOrEqualPredicate<ENTITY>
extends AbstractFieldPredicate<ENTITY, HasDoubleValue<ENTITY>>
implements HasArg0<Double> {
    
    private final double value;
    
    public DoubleGreaterOrEqualPredicate(HasDoubleValue<ENTITY> field, double value) {
        super(PredicateType.GREATER_OR_EQUAL, field, entity -> field.getAsDouble(entity) >= value);
        this.value = value;
    }
    
    @Override
    public Double get0() {
        return value;
    }
    
    @Override
    public DoubleLessThanPredicate<ENTITY> negate() {
        return new DoubleLessThanPredicate<>(getField(), value);
    }
}