import org.apache.camel.builder.RouteBuilder

class WorkflowRoute extends RouteBuilder {

    @Override
    void configure() {
        from('activemq:status').to("bean:workflowActiveService?method=status")
    }
}