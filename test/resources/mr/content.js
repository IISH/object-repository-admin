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