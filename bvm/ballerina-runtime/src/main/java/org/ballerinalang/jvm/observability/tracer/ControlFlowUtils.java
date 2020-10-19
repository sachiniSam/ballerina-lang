/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm.observability.tracer;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.observability.BallerinaObserver;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
//import org.ballerinalang.jvm.values.api.BString;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.*;

public class ControlFlowUtils {
    private static BallerinaObserver observer;
    private static final boolean tracingEnabled;


    static {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        tracingEnabled = configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED);
    }


    /**
     * Add tracing observers from TracingLaunchListener
     *
     * @param observer tracing observer
     */
    public static void addTraceObserver(BallerinaObserver observer) {
        ControlFlowUtils.observer = observer;
    }

    /**
     * Start observation for if-else statements
     *
     * @param packageID package the if-else statement belongs to
     * @param positionID The source code position the if-else block is defined in
     */
    public static void startObservation(BString packageID, BString positionID ){

        if (!tracingEnabled) {
            return;
        }

        Strand strand = Scheduler.getStrand();
        ObserverContext observerCtx = strand.observerContext;

        ObserverContext newObContext = new ObserverContext();
        newObContext.setParent(observerCtx);

        newObContext.setServiceName(observerCtx == null ? UNKNOWN_SERVICE : observerCtx.getServiceName());
        newObContext.setResourceName(observerCtx == null ? UNKNOWN_RESOURCE : observerCtx.getResourceName());

        // Need to modify function name to get the current value of if condition
        newObContext.setFunctionName("ifCondition:true");

        newObContext.addMainTag(TAG_KEY_MODULE, packageID.getValue());
        newObContext.addMainTag(TAG_KEY_INVOCATION_POSITION, positionID.getValue());
        newObContext.addMainTag(TAG_KEY_IS_IF_ELSE_BLOCK, TAG_TRUE_VALUE);

        //resource and service tags
        if (!UNKNOWN_SERVICE.equals(newObContext.getServiceName())) {
            // If service is present, resource should be too
            newObContext.addMainTag(TAG_KEY_SERVICE, newObContext.getServiceName());
            newObContext.addMainTag(TAG_KEY_RESOURCE, newObContext.getResourceName());
        }

        newObContext.setStarted(); //set setStarted method to public
        setObserverContextToCurrentFrame(strand, newObContext);

        //start only the Trace
        observer.startClientObservation(newObContext);

    }



    /**
     * Stop observation of an observer context.
     */
    public static void endObservation(){

        if (!tracingEnabled) {
            return;
        }

        Strand strand = Scheduler.getStrand();
        if (strand.observerContext == null) {
            return;
        }

        ObserverContext observerContext = strand.observerContext;

        Integer statusCode = (Integer) observerContext.getProperty(PROPERTY_KEY_HTTP_STATUS_CODE);
        if (statusCode != null && statusCode >= 100) {
            observerContext.addTag(TAG_KEY_HTTP_STATUS_CODE_GROUP, (statusCode / 100) + STATUS_CODE_GROUP_SUFFIX);
        }

        observer.stopClientObservation(observerContext);

        setObserverContextToCurrentFrame(strand, observerContext.getParent());
        observerContext.setFinished();
    }



    /**
     * Set the observer context to the current frame.
     *
     * @param strand current strand
     * @param observerContext observer context to be set
     */
    public static void setObserverContextToCurrentFrame(Strand strand, ObserverContext observerContext) {
        if (!tracingEnabled) {
            return;
        }
        strand.observerContext = observerContext;
    }
}
