import org.apache.camel.builder.RouteBuilder

class StatusRoute extends RouteBuilder {

    @Override
    void configure() {
        if (Boolean.parseBoolean(System.properties.getProperty("mq"))) {
            from('activemq:status').to("bean:workflowActiveService?method=status")
        }
    }
}