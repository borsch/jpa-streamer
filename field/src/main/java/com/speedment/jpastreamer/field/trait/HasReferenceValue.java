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
package com.speedment.jpastreamer.field.trait;

import com.speedment.jpastreamer.field.Field;
import com.speedment.jpastreamer.field.method.ReferenceGetter;

/**
 * A representation of an Entity field that is a reference type (eg 
 * {@code Integer} and not {@code int}).
 *
 * @param <ENTITY>  the entity type
 * @param <V>       the field value type
 *
 * @author  Per Minborg
 * @author  Emil Forslund
 * @since   2.2.0
 */
public interface HasReferenceValue<ENTITY, V> extends Field<ENTITY> {

    @Override
    ReferenceGetter<ENTITY, V> getter();

    /**
     * Gets the value form the Entity field.
     *
     * @param e entity
     * @return the field value
     */
    default V get(ENTITY e) {
        return getter().apply(e);
    }

}