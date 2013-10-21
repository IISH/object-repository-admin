package org.objectrepository.instruction

import org.springframework.beans.factory.DisposableBean

/**
 * WorkflowRunner
 *
 * Daemon that perpetually runs the assigned services
 *
 */
class PlanManagerService extends Thread implements Runnable, DisposableBean {

    private def services = []
    private boolean active = true
    long timeout = 10101

    public PlanManagerService(def application) {
        application.serviceClasses.each { artefact ->
            String name = artefact.name
            if (name.startsWith("Workflow")) {
                log.info "Adding " + name + " to workflow runner."
                services << application.getMainContext().getBean(artefact.clazz)
            }
        }
    }

    void run() {
        int count = services.size()
        active = count != 0
        while (active) {
            for (int i = 0; i < count; i++) {
                if (active) {
                    log.info "Running job: " + services.get(i).class.name
                    services.get(i).job()
                }
            }
            sleep(timeout)
        }
    }

    /**
     * This method is from Thread.destroy (deprecated) but invoked via DisposableBean.destroy
     */
    @SuppressWarnings("deprecation")
    public void destroy() {
        active = false
        services.clear()
    }
}
