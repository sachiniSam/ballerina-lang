/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.map;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;

/**
 * Native implementation of lang.map:get(map&lt;Type&gt;, string).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.map", functionName = "get",
//        args = {@Argument(name = "m", type = TypeKind.MAP), @Argument(name = "k", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.ANY)},
//        isPublic = true
//)
public class Get {

    public static Object get(MapValue<?, ?> m, BString k) {
        return m.getOrThrow(k);
    }
}
