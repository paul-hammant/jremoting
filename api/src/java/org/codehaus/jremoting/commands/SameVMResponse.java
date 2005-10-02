/* ====================================================================
 * Copyright 2005 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.commands;


/**
 * Class SameVMResponse
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class SameVMResponse extends Response {
    static final long serialVersionUID = -764673713874147364L;

    /**
     * Constructor a SameVMResponse
     */
    public SameVMResponse() {
    }

    /**
     * Gets number that represents type for this class.
     * This is quicker than instanceof for type checking.
     *
     * @return the representative code
     * @see org.codehaus.jremoting.commands.ResponseConstants
     */
    public int getResponseCode() {
        return ResponseConstants.SAMEVMRESPONSE;
    }
}