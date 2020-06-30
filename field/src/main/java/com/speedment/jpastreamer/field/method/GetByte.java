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
package com.speedment.jpastreamer.field.method;

import com.speedment.jpastreamer.field.trait.HasByteValue;

/**
 * A more detailed {@link ByteGetter} that also contains information about the
 * field that created it.
 * 
 * @param <ENTITY> the entity type
 *
 * @author Emil Forslund
 * @since  3.0.2
 */
public interface GetByte<ENTITY> extends ByteGetter<ENTITY> {
    
    /**
     * Returns the field that created the {@code get()}-operation.
     * 
     * @return the field
     */
    HasByteValue<ENTITY> getField();
}