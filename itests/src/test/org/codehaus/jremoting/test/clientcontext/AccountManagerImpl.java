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
package org.codehaus.jremoting.test.clientcontext;

import org.codehaus.jremoting.ClientContext;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;

import java.util.HashMap;

/**
 * @author Paul Hammant and Rune Johanessen (pairing for part)
 * @version $Revision: 1.2 $
 */

public class AccountManagerImpl implements AccountManager {

    private HashMap accounts = new HashMap();
    private ServerSideClientContextFactory clientContextFactory;

    public AccountManagerImpl(ServerSideClientContextFactory clientContextFactory, Account one, Account two) {
        this.clientContextFactory = clientContextFactory;
        accounts.put(one.getSymbolicKey(), one);
        accounts.put(two.getSymbolicKey(), two);
    }


    public void transferAmount(String acct1, String acct2, int amt) throws TransferBarfed {

        Account from = (Account) accounts.get(acct1);
        Account to = (Account) accounts.get(acct2);

        ClientContext cc = clientContextFactory.get();

        try {
            from.debit(amt);
            to.credit(amt);
        } catch (DebitBarfed debitBarfed) {
            throw new TransferBarfed();
        } catch (CreditBarfed creditBarfed) {
            throw new TransferBarfed();
        } finally {
            // ?
        }

    }
}