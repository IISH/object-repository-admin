var q = {fileSet:'/Users/lwo/object-repository/object-repository-admin/test/resources/home/12345/folder_of_cpuser/test-collection'};

var m = function () {
    this.workflow.forEach(
        function (task) {
            var success = ( task.statusCode > 799 ) ? 1 : 0;
            var failure = ( task.statusCode > 699 && task.statusCode < 800 ) ? 1 : 0;
            var time = ( success == 1 ) ? task.end - task.start : 0;
            emit(task.name, { total:1, success:success, failure:failure, time:time });
        }
    );
};

var r = function (key, values) {
    var total = 0;
    var success = 0;
    var failure = 0;
    var time = 0;
    for (var i = 0; i < values.length; i++) {
        var task = values[i];
        total += task.total;
        success += task.success;
        failure += task.failure;
        if (task.time) time += task.time;
    }
    var average = (success == 0) ? 0 : time / success;

    return { total:total, success:success, failure:failure, average:average };
};
