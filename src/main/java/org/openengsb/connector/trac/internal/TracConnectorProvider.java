package org.openengsb.connector.trac.internal;

import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.AbstractConnectorProvider;

public class TracConnectorProvider extends AbstractConnectorProvider {
    @Override
    public ServiceDescriptor getDescriptor() {
        Builder builder = ServiceDescriptor.builder(strings);
        builder.id(this.id);
        builder.name("trac.name").description("trac.description");
        builder.attribute(
            buildAttribute(builder, TracConnector.ATTRIB_USERNAME, "username.outputMode",
                "username.outputMode.description"))
            .attribute(
                builder.newAttribute()
                    .id(TracConnector.ATTRIB_PASSWORD)
                    .name("userPassword.outputMode")
                    .description(
                        "userPassword.outputMode.description")
                    .defaultValue("").asPassword().build())
            .attribute(
                builder.newAttribute()
                    .id(TracConnector.ATTRIB_SERVER)
                    .name("serverUrl.outputMode")
                    .description("serverUrl.outputMode.description")
                    .defaultValue("").required().build());

        return builder.build();
    }

    private AttributeDefinition buildAttribute(ServiceDescriptor.Builder builder, String id, String nameId,
            String descriptionId) {
        return builder.newAttribute().id(id).name(nameId)
            .description(descriptionId).defaultValue("").required().build();
    }
}
