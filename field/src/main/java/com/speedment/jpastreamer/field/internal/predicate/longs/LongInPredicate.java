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
package com.speedment.jpastreamer.field.internal.predicate.longs;

import com.speedment.jpastreamer.field.internal.predicate.AbstractFieldPredicate;
import com.speedment.jpastreamer.field.predicate.PredicateType;
import com.speedment.jpastreamer.field.trait.HasArg0;
import com.speedment.jpastreamer.field.trait.HasLongValue;

import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A predicate that evaluates if a value is included in a set of longs.
 * 
 * @param <ENTITY> entity type
 *
 * @author Emil Forslund
 * @since  3.0.0
 */
public final class LongInPredicate<ENTITY>
extends AbstractFieldPredicate<ENTITY, HasLongValue<ENTITY>>
implements HasArg0<Set<Long>> {
    
    private final Set<Long> set;
    
    public LongInPredicate(HasLongValue<ENTITY> field, Set<Long> set) {
        super(PredicateType.IN, field, entity -> set.contains(field.getAsLong(entity)));
        this.set = requireNonNull(set);
    }
    
    @Override
    public Set<Long> get0() {
        return set;
    }
    
    @Override
    public LongNotInPredicate<ENTITY> negate() {
        return new LongNotInPredicate<>(getField(), set);
    }
}