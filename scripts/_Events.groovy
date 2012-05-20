eventCompileStart = { msg ->
    String hash = ("git rev-parse HEAD".execute().text)
    String href = '<a target="_blank" href="https://github.com/IISH/object-repository-admin/commit/' + hash + '">Build</a>'
    new File("grails-app/views/_git.gsp").text = href
}

eventDocEnd = {  kind ->

    if (kind == "refdocs") {
        def fromDir = "${basedir}/target/docs"
        def toDir = "${basedir}/web-app/doc/latest"
        new AntBuilder().copy(todir: toDir) {
            fileset(dir: fromDir) {
                //include(name: "**/*.java")
                //exclude(name: "**/*Test.java")
            }
        }
    }
}