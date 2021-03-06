/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ode.bpel.eapi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class that bundles and registers <code>&lt;extensionActivity&gt;</code> and
 * <code>&lt;extensionAssignOperation&gt;</code> implementations related to a particular namespace.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public abstract class AbstractExtensionBundle {
    private Map<String, Class<? extends ExtensionOperation>> extensionsByName =
            new HashMap<String, Class<? extends ExtensionOperation>>();

    /**
     * Returns the extension namespace this bundle provides implementations for.
     * 
     * @return
     */
    public abstract String getNamespaceURI();

    /**
     * Register extension operations.
     */
    public abstract void registerExtensionActivities();

    /**
     * Register an {@link ExtensionOperation} implementation as
     * <code>&lt;extensionActivity&gt;</code>.
     *
     * @param localName
     * @param activity
     */
    protected final void registerExtensionOperation(String localName,
            Class<? extends ExtensionOperation> operation) {
        extensionsByName.put(localName, operation);
    }

    /**
     * Returns a list of the local names of registered extension operations.
     */
    public final Set<String> getExtensionOperationNames() {
        return Collections.unmodifiableSet(extensionsByName.keySet());
    }

    public final Class<? extends ExtensionOperation> getExtensionOperationClass(String localName) {
        return extensionsByName.get(localName);
    }

    public final ExtensionOperation getExtensionOperationInstance(String localName)
            throws InstantiationException, IllegalAccessException {
        return getExtensionOperationClass(localName).newInstance();
    }

}
