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

import com.speedment.jpastreamer.field.method.BooleanGetter;
import com.speedment.jpastreamer.field.method.GetBoolean;
import com.speedment.jpastreamer.field.trait.HasBooleanValue;

import static java.util.Objects.requireNonNull;

/**
 * Default implementation of the {@link GetBoolean}-interface.
 * 
 * @param <ENTITY> the entity type
 *
 * @author Emil Forslund
 * @since  3.0.2
 */
public final class GetBooleanImpl<ENTITY> implements GetBoolean<ENTITY> {
    
    private final HasBooleanValue<ENTITY> field;
    private final BooleanGetter<ENTITY> getter;
    
    public GetBooleanImpl(HasBooleanValue<ENTITY> field, BooleanGetter<ENTITY> getter) {
        this.field  = requireNonNull(field);
        this.getter = requireNonNull(getter);
    }
    
    @Override
    public HasBooleanValue<ENTITY> getField() {
        return field;
    }
    
    @Override
    public boolean applyAsBoolean(ENTITY instance) {
        return getter.applyAsBoolean(instance);
    }
}