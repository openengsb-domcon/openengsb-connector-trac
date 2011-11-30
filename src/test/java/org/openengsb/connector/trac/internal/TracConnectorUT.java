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
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;
import org.openengsb.connector.trac.internal.models.TicketHandlerFactory;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.issue.IssueDomainEvents;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.Priority;
import org.openengsb.domain.issue.models.Status;

public class TracConnectorUT {

    @Test
    public void testCreateIssue() throws XmlRpcException {
        TicketHandlerFactory ticketFactory = new TicketHandlerFactory();
        ticketFactory.setUsername("");
        ticketFactory.setUserPassword("");
        ticketFactory.setServerUrl("http://127.0.0.1:8000/myTracProj/xmlrpc");
        TracConnector tracImpl = new TracConnector("1", ticketFactory);
        
        IssueDomainEvents domainEvents = mock(IssueDomainEvents.class);
        tracImpl.setIssueEvents(domainEvents);
        
        Issue issue = ModelUtils.createEmptyModelObject(Issue.class);
        issue.setDescription("test Issue");
        issue.setSummary("test summery");
        issue.setId("99");
        issue.setStatus(Status.NEW);
        issue.setPriority(Priority.NONE);
        String id = tracImpl.createIssue(issue);

        assertThat(id, not(is("-1")));
    }
}
