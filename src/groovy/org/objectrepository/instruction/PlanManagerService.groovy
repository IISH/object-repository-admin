package org.objectrepository.instruction

/**
 * WorkflowRunner
 *
 * Daemon that perpetually runs the assigned services
 *
 */
class PlanManagerService extends Thread implements Runnable {

    private def services = []
    private boolean active = true
    long timeout = 10000

    public PlanManagerService(def application) {
        application.serviceClasses.each { artefact ->
            String name = artefact.name
            if (name.startsWith("Workflow")) {
                println("Adding " + name + " to workflow runner.")
                services << application.getMainContext().getBean(artefact.clazz)
            }
        }
    }

    public void cleanup() {
        active = false
        services.clear()
    }

    void run() {
        int count = services.size()
        active = count != 0
        while (active) {
            for (int i = 0; i < count; i++) {
                if (active) try {
                    services.get(i).job()
                } catch (Exception e) {
                    println(e.message)
                }
            }
            sleep(timeout)
        }
    }
}
