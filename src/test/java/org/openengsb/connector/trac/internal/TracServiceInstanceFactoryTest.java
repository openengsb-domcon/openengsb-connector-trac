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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openengsb.connector.trac.internal.models.TicketHandlerFactory;
import org.openengsb.core.api.Connector;

public class TracServiceInstanceFactoryTest {

    @Test
    public void testCreateTracConnector_ShouldReturnTracConnector() {
        TracServiceInstanceFactory factory = new TracServiceInstanceFactory();
        Map<String, String> attributes = new HashMap<String, String>();

        Connector tracConnector = factory.createNewInstance("id1");
        factory.applyAttributes(tracConnector, attributes);
        assertThat(tracConnector.getInstanceId(), is("id1"));
    }

    @Test
    public void testUpdateTracConnector() {
        TracServiceInstanceFactory factory = new TracServiceInstanceFactory();

        TracConnector tracConnector = mock(TracConnector.class);
        TicketHandlerFactory tc = mock(TicketHandlerFactory.class);
        when(tracConnector.getTicketHandlerFactory()).thenReturn(tc);

        Map<String, String> newAttributes = new HashMap<String, String>();
        newAttributes.put(TicketHandlerFactory.ATTRIB_SERVER, "newUrl");
        newAttributes.put(TicketHandlerFactory.ATTRIB_USERNAME, "newUser");
        newAttributes.put(TicketHandlerFactory.ATTRIB_PASSWORD, "newPassword");

        factory.applyAttributes(tracConnector, newAttributes);
        verify(tc, times(1)).setServerUrl("newUrl");
        verify(tc, times(1)).setUsername("newUser");
        verify(tc, times(1)).setUserPassword("newPassword");
    }

    @Test
    public void getServiceDescriptor_ShouldReturnTracConnectorServiceDescriptor() {

    }
}
