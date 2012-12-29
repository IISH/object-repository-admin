import org.apache.camel.builder.RouteBuilder

class DlqRoute extends RouteBuilder {

    @Override
    void configure() {
        if (Boolean.parseBoolean(System.properties.getProperty("plans"))) {
            from('activemq:ActiveMQ.DLQ').to("bean:workflowActiveService?method=dlq")
        }
    }
}