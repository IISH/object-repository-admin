package org.objectrepository.instruction

import com.mongodb.BasicDBObject
import com.mongodb.MapReduceCommand
import org.objectrepository.util.OrUtil

/**
 * Tasking
 *
 * Profiles the getter and setter and accessibility for the primary task in a workflow
 */
abstract class Tasking {

    def mongo
    def cacheTask = null

    Task getTask() {
        OrUtil.takeFirst(workflow)
    }

    void setTask(def map) {
        setTask(new Task(map.properties))
    }

    void setTask(Task _task) {
        if (_task.name) {
            workflow.push(_task)
        }
    }

    /**
     * getTasks
     *
     * Starts a mapreduce to collate all statusCode per workflow
     * Not very fast if the fileSet is large.
     *
     * @return
     */
    List getTasks() {
        
        final c = mongo.getDB('sa').stagingfile
        MapReduceCommand mapReduceCommand = new MapReduceCommand(c,
                """
            function map() {
                this.workflow.forEach(
                        function (task) {
                            var success = ( task.statusCode > 799 && task.statusCode != 850) ? 1 : 0;
                            var failure = ( task.statusCode > 699 && task.statusCode < 800 || task.statusCode == 850) ? 1 : 0;
                            var processed = ( success == 1 || failure == 1 ) ? 1 : 0;
                            var time = ( success == 1 ) ? task.end - task.start : 0;
                            emit(task.name, { total:1, success:success, failure:failure, processed:processed, time:time });
                        }
                    );
            }
            """,
                """
            function reduce(key, values) {
                var total = 0;
                    var success = 0;
                    var failure = 0;
                    var processed = 0;
                    var time = 0;
                    for (var i = 0; i < values.length; i++) {
                        var task = values[i];
                        total += task.total;
                        success += task.success;
                        failure += task.failure;
                        processed += task.processed;
                        time += task.time;
                    }
                    var average = ( success == 0 ) ? 0 : time / success;

                    return { total:total, success:success, failure:failure, processed:processed, average:average };
            }
            """,
                null,
                MapReduceCommand.OutputType.INLINE,
                new BasicDBObject("fileSet", fileSet)
        )
        c.mapReduce(mapReduceCommand).results().collect {
            [
                    name: it._id,
                    success: it.value.success as Integer,
                    failure: it.value.failure as Integer,
                    total: it.value.total as Integer,
                    processed: it.value.processed as Integer,
                    average: it.value.average
            ]
        }
    }

    boolean getIngesting() {
        task.name == 'InstructionIngest' && task.statusCode == 800
    }
}
