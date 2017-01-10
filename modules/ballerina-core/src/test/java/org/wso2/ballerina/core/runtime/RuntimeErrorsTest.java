/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetString;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Runtime Errors test class for ballerina filers.
 * This class test error handling for runtime errors in ballerina files.
 */
public class RuntimeErrorsTest {
    
    private BallerinaFile bFile;
    
    @BeforeClass
    public void setup() {
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        FunctionUtils.addNativeFunction(symScope, new GetString());
        bFile = ParserUtils.parseBalFile("lang/runtime-errors.bal", symScope);
    }

    @Test(expectedExceptions = {BallerinaException.class },
            expectedExceptionsMessageRegExp = "Array index out of range: Index: 5, Size: 2")
    public void testArrayIndexOutOfBoundError() {
        Functions.invoke(bFile, "arrayIndexOutOfBoundTest");
    }
    
    @Test
    public void testStackTraceOnError() {
        Exception ex = null;
        Context bContext = new Context();
        String expectedStackTrace = "\t at test.lang:getApple(runtime-errors.bal:23)\n" +
                "\t at test.lang:getFruit2(runtime-errors.bal:19)\n" +
                "\t at test.lang:getFruit1(runtime-errors.bal:15)\n" +
                "\t at test.lang:testStackTrace(runtime-errors.bal:12)\n";
        try {
            Functions.invoke(bFile, "testStackTrace", bContext);
        } catch (BallerinaException e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                ", but found: " + ex + ".");
            Assert.assertEquals(ex.getMessage(), "Array index out of range: Index: 24, Size: 0", 
                    "Incorrect error message printed.");
            
            // removing the first element since we are not invoking a main function
            bContext.getControlStack().getStack().remove(0);
            
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getBallerinaStackTrace(bContext);
            Assert.assertEquals(stackTrace, expectedStackTrace);
        }
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "Failed to get string from json. Invalid jsonpath: Path must not end " +
            "with a '.' or '..'")
    public void testNativeFunctionError() {
        Functions.invoke(bFile, "nativeFunctionErrorTest");
    }

}