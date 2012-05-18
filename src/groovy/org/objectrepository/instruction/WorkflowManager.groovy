package org.objectrepository.instruction

/**
 * WorkflowRunner
 *
 * Daemon that perpetually runs the assigned workflows
 *
 */
class WorkflowManager extends Thread implements Runnable {

    def workflows = []
    private boolean active = true
    long timeout = 10000

    public WorkflowManager(def application) {
        application.serviceClasses.each { artefact ->
            String name = artefact.name
            if (name.startsWith("Workflow")) {
                println("Adding " + name + " to workflow runner.")
                workflows << application.getMainContext().getBean(artefact.clazz)
            }
        }
    }

    public void cleanup() {
        active = false
        workflows.clear()
    }

    void run() {
        int count = workflows.size()
        active = count != 0
        while (active) {
            for (int i = 0; i < count; i++) {
                if (active) workflows.get(i).job()
            }
            sleep(timeout)
        }
    }
}
