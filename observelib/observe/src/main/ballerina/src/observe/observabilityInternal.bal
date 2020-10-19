// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/java;

# Start a Trace span for the If-else blocks
#
# + packageID - Package details of the if-else statement
# + positionID - Column and row position detail of the start of if-else statements
public function startObservation(string packageID, string positionID) = @java:Method {
    name: "startObservation",
    'class: "org.ballerinalang.jvm.observability.tracer.ControlFlowUtils"
} external;

# End the Trace span for the if-else block
public function endObservation() = @java:Method {
    name: "endObservation",
    'class: "org.ballerinalang.jvm.observability.tracer.ControlFlowUtils"
} external;

