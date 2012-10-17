autoGeneratePIDs = [
        'none', 'uuid', 'lid', 'filename2pid', 'filename2lid'
]

action = ['upsert', 'add', 'update', 'delete']

/**
 * Place methods for determining if the service should be offered in the service.method key
 */
plans = [
        UploadFiles: [ // fileSet and files... but not yet an instruction
                statusCodes: [
                        // In the beginning, there is nothing
                        000: [purpose: 'A fileset appeared'],
                        800: [purpose: 'Files are in the fileset. The user now needs to add an instruction']
                ]
        ],
        InstructionAutocreate: [
                service: [method: 'hasFSFiles'],
                statusCodes: [
                        100: [purpose: 'A user has made a request to automatically generate an instruction'],
                        200: [purpose: 'A request is being to the message queue to generate an instruction'],
                        300: [purpose: 'A request to produce an instruction is on the queue'],
                        400: [purpose: 'The request is taken from the queue. The instruction is being created'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Validating if the task was successfull'],
                        700: [purpose: 'There was an error.'],
                        710: [purpose: 'This service was started, but there are no files on the staging area.'],
                        800: [purpose: 'Instruction ready']
                ]
        ],
        InstructionDownload: [
                service: [method: 'hasDBInstruction', ingest: ['pending', 'working']],
                statusCodes: [
                        100: [purpose: 'A user asked to download an instruction.']
                ]
        ],
        InstructionUpload: [
                service: [method: 'hasFSFiles'],
                statusCodes: [
                        100: [purpose: 'A user or system has made a request to declare a custom instruction for the fileSet'],
                        200: [purpose: 'Sending declaration to the queue'],
                        300: [purpose: 'A request to read in this instruction is on the queue'],
                        400: [purpose: 'The request is taken from the queue. Importing an instruction'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Validating if the task was successfull'],
                        700: [purpose: 'There was an error.'],
                        800: [purpose: 'Instruction read in.']
                ]
        ],
        InstructionValidate: [
                service: [method: 'hasDBInstruction'],
                statusCodes: [
                        100: [purpose: 'The user has made a request to validate this instruction'],
                        200: [purpose: 'Sending declaration to the queue'],
                        300: [purpose: 'The instruction has been sent to the queue for processing'],
                        400: [purpose: 'The request is taken from the queue. Validating an instruction'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Check if the task was successfull'],
                        700: [purpose: 'An error occured when validating the files'],
                        800: [purpose: 'The overal instruction has been validated. All is well.']
                ]
        ],
        InstructionIngest: [
                service: [method: 'hasValidDBInstruction', statusCode: 800],
                statusCodes: [
                        100: [purpose: 'The user has made a request to ingest this instruction'],
                        200: [purpose: 'Sending declaration to the queue'],
                        300: [purpose: 'The instruction has been sent to the queue for processing'],
                        400: [purpose: 'Ingesting files'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Check if the ingest command was completed'],
                        700: [purpose: 'Something went wrong'],
                        800: [purpose: 'Instruction is ongoing. Files are now processed individualy']
                ],
        ], InstructionRetry: [
        service: [method: 'hasFailedTasks', statusCode: 900],
        statusCodes: [
                100: [purpose: 'The user has made a request to retry failed tasks in this instruction']
        ],
],
        Start: [
                statusCodes: [
                        000: [purpose: 'The file is ready for ingest'],
                        100: [purpose: 'The file is ready for ingest and will be processed now'],
                        800: [purpose: 'The file is ready for ingest and will be processed now']
                ]
        ],
        StagingfileIngestMaster: [
                task: [limit: Integer.MAX_VALUE],
                statusCodes: [
                        100: [purpose: 'The system received a request to ingest this master'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The master\'s location has been sent to the queue for ingestation'],
                        400: [purpose: 'Ingesting master file'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if the master was inserted into the database'],
                        800: [purpose: 'Master file is inserted']
                ],
        ],
        StagingfileBindPIDs: [
                statusCodes: [
                        100: [purpose: 'The system received a request to create or bind PIDs'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The command to bind PIDs has been sent to the queue'],
                        400: [purpose: 'Binding PIDs with the repositories disseminiation API ( website )'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verify the existamce of PIDs'],
                        700: [purpose: 'We could not verify the bindings of the PID'],
                        800: [purpose: 'PID are bound to the resolve URLs']
                ]
        ],
        StagingfileIngestCustomLevel1: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel1: [
                method: 'renameQueueWithContentType',
                executeAfter: 'StagingfileIngestCustomLevel1',
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestCustomLevel2: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel2: [
                method: 'renameQueueWithContentType',
                executeAfter: 'StagingfileIngestCustomLevel2',
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestCustomLevel3: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ]
        ,
        StagingfileIngestLevel3: [
                method: 'renameQueueWithContentType',
                executeAfter: 'StagingfileIngestCustomLevel3',
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored,']
                ]
        ],
        StagingfileIngestLevel1Image: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel2Image: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel3Image: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored,']
                ]
        ]
        ,
        StagingfileIngestLevel1Audio: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel2Audio: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel3Audio: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored,']
                ]
        ],
        StagingfileIngestLevel1Video: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel2Video: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored']
                ],
        ],
        StagingfileIngestLevel3Video: [
                visible: false,
                statusCodes: [
                        100: [purpose: 'The system received a request to produce this derivative'],
                        200: [purpose: 'Sending request to the queue'],
                        300: [purpose: 'The object\'s location has been sent to the queue for derivative creation'],
                        400: [purpose: 'Creating derivative'],
                        500: [purpose: 'The service node completed the task.'],
                        600: [purpose: 'Verifying if the task was successfull.'],
                        700: [purpose: 'We could not see if a derivative was created'],
                        800: [purpose: 'Derivate file is produced and stored,']
                ]
        ]
        ,
        EndOfTheRoad: [
                statusCodes: [
                        100: [purpose: 'End of the factory line. The file can now be removed'],
                        800: [purpose: 'final']
                ]
        ],
        FileRemove: [
                statusCodes: [
                        100: [purpose: 'Removal of a file ( or file reference ) from the repository.'],
                        400: [purpose: 'Removing file'],
                        800: [purpose: 'The file has been locked', action: 'EndOfTheRoad']
                ]
        ],
        TestTaskLevelA: [ // Tests
                statusCodes: [
                        000: [purpose: 'Level 1 Some moment'],
                        100: [purpose: 'Level 1 Another momenTaskt'],
                        200: [purpose: 'Level 1 Yet another moment'],
                        800: [purpose: 'Level 1 The last moment']
                ]
        ],
        TestTaskLevelB: [ // fileSet and files... but not yet an instruction
                statusCodes: [
                        000: [purpose: 'Level 2 Some moment'],
                        100: [purpose: 'Level 2 Another momenTaskt'],
                        200: [purpose: 'Level 2 Yet another moment'],
                        800: [purpose: 'Level 2 The last moment']
                ]
        ],
        TestTaskLevelC: [
                statusCodes: [
                        100: [purpose: 'Level 3 Some moment'],
                        200: [purpose: 'Level 3 Yet another moment'],
                        500: [purpose: 'Level 3 Yet another moment'],
                        600: [purpose: 'Level 3 Yet another moment'],
                        900: [purpose: 'Level 3 The last moment']
                ]
        ]
]