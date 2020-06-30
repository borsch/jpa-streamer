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
package com.speedment.jpastreamer.field.internal.method;

import com.speedment.jpastreamer.field.method.GetLong;
import com.speedment.jpastreamer.field.method.LongGetter;
import com.speedment.jpastreamer.field.trait.HasLongValue;

import static java.util.Objects.requireNonNull;

/**
 * Default implementation of the {@link GetLong}-interface.
 * 
 * @param <ENTITY> the entity type
 *
 * @author Emil Forslund
 * @since  3.0.2
 */
public final class GetLongImpl<ENTITY> implements GetLong<ENTITY> {
    
    private final HasLongValue<ENTITY> field;
    private final LongGetter<ENTITY> getter;
    
    public GetLongImpl(HasLongValue<ENTITY> field, LongGetter<ENTITY> getter) {
        this.field  = requireNonNull(field);
        this.getter = requireNonNull(getter);
    }
    
    @Override
    public HasLongValue<ENTITY> getField() {
        return field;
    }
    
    @Override
    public long applyAsLong(ENTITY instance) {
        return getter.applyAsLong(instance);
    }
}