import org.apache.camel.builder.RouteBuilder

class WorkflowRoute extends RouteBuilder {

    @Override
    void configure() {
        if (Boolean.parseBoolean(System.properties.getProperty("plans"))) {
            from('activemq:status').to("bean:workflowActiveService?method=status")
        }
    }
}