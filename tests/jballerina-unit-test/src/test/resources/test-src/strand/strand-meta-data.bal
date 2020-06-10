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
import ballerina/io;
import ballerina/runtime;

int totalNoOfStrandsForTest = 14;
int errorCount = 0;
int successCount = 0;
string[] errorMessages = [];

type Person object {
    public string name;
    function __init(string name) {
        // async calls inside object
        _ = start assertStrandMetadataResult("$anon/.:0.0.0.Person.__init");
        worker w2 {
            assertStrandMetadataResult("$anon/.:0.0.0.Person.__init.w2");
        }
         _ =  @strand{name:"**my strand inside object**"}
                        start assertStrandMetadataResult("$anon/.:0.0.0.Person.__init.**my strand inside object**");
        foo();
        self.name = name;
        // object function
        self.bar();
    }

    function bar() {
         assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls.test");
         // async call inside object function
          _ =  @strand{name:"**my strand inside object bar**"}
                start assertStrandMetadataResult("$anon/.:0.0.0.Person.bar.**my strand inside object bar**");
    }
};


function testStrandMetadataAsyncCalls() {
    Person p1 = new("Waruna");
    // inside same function
    assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls.test");
    // inside function call
    foo();
    // worker
    worker w1 {
        assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls.w1");
    }
    // async function call
    future<()> f1 = start assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls.f1");

    // anonymous async call
    _ = start assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls");
    _ = start assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls");

    // async call with strand name
    _ = @strand{name:"**my strand**"}
            start assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls.**my strand**");

    // async function pointer
    function(string s) func = assertStrandMetadataResult;
    future<()> x = start func("$anon/.:0.0.0.testStrandMetadataAsyncCalls.x");

    // Wait until all the async calls are done
    while (successCount < (totalNoOfStrandsForTest*2) && errorCount == 0) {
        runtime:sleep(1);
    }
    if (errorCount > 0) {
        errorMessages.forEach(function(string message) {
            io:println(message);
        });
        panic AssertionError(message = "Test failed due to errors.");
    }
}

function foo() {
    assertStrandMetadataResult("$anon/.:0.0.0.testStrandMetadataAsyncCalls.test");
    worker w1 {
        assertStrandMetadataResult("$anon/.:0.0.0.foo.w1");
    }
}

function assertStrandMetadataResult(string assertString) {
    string result = "";
    var strand = getStrand();
    var metadata = getMetaData(strand);
    if (nonNull(metadata)) {
        int id = getId(strand);
        var strandName = getName(strand);
        var isStrandHasName = isPresent(strandName);
        string org = <string>java:toString(getModuleOrg(metadata));
        string modName = <string>java:toString(getModuleName(metadata));
        string modVersion = <string>java:toString(getModuleVersion(metadata));
        string parentFunc = <string>java:toString(getParentFunctionName(metadata));
        string typeName = "";
        var typeNameVal = java:toString(getTypeName(metadata));
        string name = "";
        if (isStrandHasName) {
            name = "."+ <string>java:toString(get(strandName));
        }
        if (typeNameVal is string) {
            typeName = "." + typeNameVal;
        }
        assertEquality(org +"/" + modName + ":" + modVersion + typeName + "." + parentFunc + name, assertString);
        assertTrue(id > 0);
    } else {
        errorMessages[errorCount] = "meta data cannot be found for evaluate assert string '" + assertString + "'";
        errorCount = errorCount + 1;
    }
}

function getName(handle strand) returns handle = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Strand"
} external;

function get(handle optional) returns handle = @java:Method {
    class: "java.util.Optional"
} external;

function nonNull(handle optional) returns boolean = @java:Method {
    class: "java.util.Objects"
} external;

function isPresent(handle optional) returns boolean = @java:Method {
    class: "java.util.Optional"
} external;

function getId(handle strand) returns int = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Strand"
} external;

function getStrand() returns handle = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Scheduler"
} external;

function getMetaData(handle strand) returns handle = @java:Method {
    class: "org.ballerinalang.jvm.scheduling.Strand"
} external;

function getModuleOrg(handle strandMetaData) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetadata",
    name : "moduleOrg"
} external;

function getModuleName(handle strandMetaData) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetadata",
     name : "moduleName"
} external;

function getModuleVersion(handle strandMetaData) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetadata",
    name : "moduleVersion"
} external;

function getParentFunctionName(handle strandMetaData) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetadata",
    name : "parentFunctionName"
} external;

function getTypeName(handle strandMetaData) returns handle = @java:FieldGet {
    class: "org.ballerinalang.jvm.scheduling.StrandMetadata",
    name : "typeName"

} external;

type AssertionError error<ASSERTION_ERROR_REASON>;

const ASSERTION_ERROR_REASON = "AssertionError";

public function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

public function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        successCount = successCount + 1;
        return;
    }
    errorMessages[errorCount] = "expected '" + expected.toString() + "', found '" + actual.toString() + "'";
    errorCount = errorCount + 1;
}
