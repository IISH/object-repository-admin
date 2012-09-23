package org.objectrepository.stats

import com.mongodb.BasicDBObject
import com.mongodb.MapReduceCommand
import org.objectrepository.instruction.Stagingfile

class StatisticsService {

    def grailsApplication
    def mongo

    List getTasks(String na) {

        MapReduceCommand mapReduceCommand = new MapReduceCommand(Stagingfile.collection,
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
                new BasicDBObject("na", na)
        )
        Stagingfile.collection.mapReduce(mapReduceCommand).results().collect {
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

    /**
     * getStats
     *
     * Get a list of stats from our db.
     *
     * @param na
     * @return
     */
    def getStats(String na) {


        def stats = grailsApplication.config.accessMatrix.closed.collect {
            it.bucket
        }.collect {
            final collectionFiles = mongo.getDB('or_' + na).getCollection(it + ".files").getStats()
            final collectionChunks = mongo.getDB('or_' + na).getCollection(it + ".chunks").getStats()
            [
                    bucket: it,
                    count: collectionFiles.count,
                    storageSize: collectionChunks.storageSize + collectionFiles.storageSize
            ]
        }
        long count = 0, storageSize = 0
        stats.each {
            count += it.count
            storageSize += it.storageSize
        }
        stats << [bucket: 'replica', count: count, storageSize: storageSize]
        stats << [bucket: 'total', count: count * 2, storageSize: storageSize * 2]
        stats
    }
}
