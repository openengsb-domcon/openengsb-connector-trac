/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.trac.internal;

import java.util.Map;

import org.openengsb.connector.trac.internal.models.TicketHandlerFactory;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ekb.PersistInterface;
import org.openengsb.core.common.AbstractConnectorInstanceFactory;

public class TracServiceInstanceFactory extends
        AbstractConnectorInstanceFactory<TracConnector> {
    
    private PersistInterface persistInterface;

    public Connector createNewInstance(String id) {
        TicketHandlerFactory ticketFactory = new TicketHandlerFactory();
        TracCommitHandler handler = new TracCommitHandler();
        handler.setPersistInterface(persistInterface);
        TracConnector tracConnector = new TracConnector(id, ticketFactory);
        tracConnector.setCommitHandler(handler);
        return tracConnector;
    };

    @Override
    public void doApplyAttributes(TracConnector instance, Map<String, String> attributes) {
        TicketHandlerFactory ticketFactory = instance.getTicketHandlerFactory();
        updateTicketHandlerFactory(attributes, ticketFactory);
    }

    private void updateTicketHandlerFactory(Map<String, String> attributes, TicketHandlerFactory ticketFactory) {
        if (attributes.containsKey(TicketHandlerFactory.ATTRIB_SERVER)) {
            ticketFactory.setServerUrl(attributes.get(TicketHandlerFactory.ATTRIB_SERVER));
        }
        if (attributes.containsKey(TicketHandlerFactory.ATTRIB_USERNAME)) {
            ticketFactory.setUsername(attributes.get(TicketHandlerFactory.ATTRIB_USERNAME));
        }
        if (attributes.containsKey(TicketHandlerFactory.ATTRIB_PASSWORD)) {
            ticketFactory.setUserPassword(attributes.get(TicketHandlerFactory.ATTRIB_PASSWORD));
        }
    }
    
    public void setPersistInterface(PersistInterface persistInterface) {
        this.persistInterface = persistInterface;
    }
}
