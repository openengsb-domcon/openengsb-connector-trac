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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.openengsb.connector.trac.internal.models.TicketHandlerFactory;
import org.openengsb.connector.trac.internal.models.constants.TracFieldConstants;
import org.openengsb.connector.trac.internal.models.constants.TracPriorityConstants;
import org.openengsb.connector.trac.internal.models.constants.TracStatusConstants;
import org.openengsb.connector.trac.internal.models.xmlrpc.Ticket;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.DomainMethodNotImplementedException;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.domain.issue.IssueDomain;
import org.openengsb.domain.issue.models.Issue;
import org.openengsb.domain.issue.models.IssueAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracConnector extends AbstractOpenEngSBService implements IssueDomain {

    public static final String ATTRIB_SERVER = "serverUrl";
    public static final String ATTRIB_PASSWORD = "userPassword";
    public static final String ATTRIB_USERNAME = "username";

    private static final Logger LOGGER = LoggerFactory.getLogger(TracConnector.class);

    private AliveState state = AliveState.DISCONNECTED;
    private final TicketHandlerFactory ticketFactory;

    public TracConnector(String id, TicketHandlerFactory ticketFactory) {
        super(id);
        this.ticketFactory = ticketFactory;
    }

    @Override
    public String createIssue(Issue issue) {
        Ticket ticket = createTicket();
        Hashtable<IssueAttribute, String> attributes = generateAttributes(issue);
        String issueId = "-1";

        try {
            issueId = ticket.create(issue.getSummary(), issue.getDescription(), attributes).toString();
            state = AliveState.ONLINE;
            LOGGER.info("Successfully created issue {}, ID is: {}.", issue.getSummary(), issueId);
        } catch (XmlRpcException e) {
            LOGGER.error("Error creating issue {}. XMLRPC call failed.", issue.getSummary());
            state = AliveState.OFFLINE;
        }
        return issueId.toString();
    }

    public void deleteIssue(String id) {
        try {
            Ticket ticket = createTicket();
            ticket.delete(Integer.valueOf(id));
            LOGGER.info("Successfully deleted issue {}.", id);
        } catch (XmlRpcException e) {
            LOGGER.error("Error deleting issue {}. XMLRPC call failed.", id);
        }
    }

    @Override
    public void addComment(String id, String comment) {
        try {
            Ticket ticket = createTicket();
            ticket.update(Integer.valueOf(id), comment);
            LOGGER.info("Successfully added comment to issue {}.", id);
        } catch (XmlRpcException e) {
            LOGGER.error("Error adding comment to issue {}. XMLRPC call failed.", id);
        }
    }

    @Override
    public void updateIssue(String id, String comment, HashMap<IssueAttribute, String> changes) {
        Hashtable<IssueAttribute, String> attributes = translateChanges(changes);
        if (comment == null || comment.equals("")) {
            comment = "[No comment added by author]";
        }

        try {
            Ticket ticket = createTicket();
            ticket.update(Integer.valueOf(id), comment, attributes);
            LOGGER.info("Successfully updated issue {} with {} changes.", id, changes.size());
        } catch (XmlRpcException e) {
            LOGGER.error("Error updating issue {}. XMLRPC call failed.", id);
        }
    }

    @Override
    public void moveIssuesFromReleaseToRelease(String releaseFromId, String releaseToId) {
        throw new DomainMethodNotImplementedException("method not yet implemented");
    }

    @Override
    public void closeRelease(String id) {
        throw new DomainMethodNotImplementedException("method not yet implemented");
    }

    @Override
    public ArrayList<String> generateReleaseReport(String releaseId) {
        throw new DomainMethodNotImplementedException("method not yet implemented");
    }

    private Ticket createTicket() {
        if (ticketFactory != null) {
            Ticket ticket = ticketFactory.createTicket();
            if (ticket != null) {
                state = AliveState.CONNECTING;
            } else {
                state = AliveState.DISCONNECTED;
            }
            return ticket;
        }
        throw new RuntimeException("tickethandler not yet set");
    }

    public TicketHandlerFactory getTicketHandlerFactory() {
        return ticketFactory;
    }

    private Hashtable<IssueAttribute, String> translateChanges(Map<IssueAttribute, String> changes) {
        Hashtable<IssueAttribute, String> attributes = new Hashtable<IssueAttribute, String>();

        for (Map.Entry<IssueAttribute, String> entry : changes.entrySet()) {
            try {
                if (entry.getKey().equals(Issue.Field.DESCRIPTION)) {
                    attributes.put(TracFieldConstants.DESCRIPTION, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.OWNER)) {
                    attributes.put(TracFieldConstants.OWNER, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.REPORTER)) {
                    attributes.put(TracFieldConstants.SUMMARY, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.SUMMARY)) {
                    attributes.put(TracFieldConstants.SUMMARY, entry.getValue());
                } else if (entry.getKey().equals(Issue.Field.PRIORITY)) {
                    addPriority(attributes, Issue.Priority.valueOf(entry.getValue()));
                } else if (entry.getKey().equals(Issue.Field.STATUS)) {
                    addStatus(attributes, Issue.Status.valueOf(entry.getValue()));
                }
            } catch (ClassCastException e) {
                LOGGER.error(
                    "Wrong value provided for field {}: {}", entry.getKey(), entry.getValue().getClass().getName());
            }
        }

        return attributes;
    }

    private Hashtable<IssueAttribute, String> generateAttributes(Issue issue) {
        Hashtable<IssueAttribute, String> attributes = new Hashtable<IssueAttribute, String>();

        if (issue.getOwner() != null) {
            attributes.put(TracFieldConstants.OWNER, issue.getOwner());
        }
        if (issue.getReporter() != null) {
            attributes.put(TracFieldConstants.REPORTER, issue.getReporter());
        }

        addPriority(attributes, issue.getPriority());
        addStatus(attributes, issue.getStatus());

        return attributes;
    }

    private void addPriority(Hashtable<IssueAttribute, String> attributes, Issue.Priority priority) {
        if (priority != null) {
            if (priority.equals(Issue.Priority.HIGH)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.HIGH.toString());
            } else if (priority.equals(Issue.Priority.IMMEDIATE)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.IMMEDIATE.toString());
            } else if (priority.equals(Issue.Priority.LOW)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.LOW.toString());
            } else if (priority.equals(Issue.Priority.NORMAL)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.NORMAL.toString());
            } else if (priority.equals(Issue.Priority.URGEND)) {
                attributes.put(TracFieldConstants.PRIORITY, TracPriorityConstants.URGENT.toString());
            }
        }
    }

    private void addStatus(Hashtable<IssueAttribute, String> attributes, Issue.Status status) {
        if (status != null) {
            if (status.equals(Issue.Status.NEW)) {
                attributes.put(TracFieldConstants.STATUS, TracStatusConstants.NEW.toString());
            } else if (status.equals(Issue.Status.ASSIGNED)) {
                attributes.put(TracFieldConstants.STATUS, TracStatusConstants.ASSIGNED.toString());
            } else if (status.equals(Issue.Status.CLOSED)) {
                attributes.put(TracFieldConstants.STATUS, TracStatusConstants.CLOSED.toString());
            }
        }
    }

    @Override
    public AliveState getAliveState() {
        return state;
    }
}
