grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.offline.mode = false

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums false // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // group, name, version

        compile("org.quartz-scheduler:quartz:1.8.4") {
            excludes 'xml-apis', 'commons-logging', 'slf4j-api'
        }
        compile("org.codehaus.groovy.modules.http-builder:http-builder:0.5.2")
        compile('org.springframework.security.oauth:spring-security-oauth:1.0.0.M3') {
            excludes "spring-security-core", "spring-security-web", "commons-codec"
        }
    }

    plugins {
        runtime ":jquery:1.7.1"
        runtime ":resources:1.1.6"
        build ":tomcat:$grailsVersion"
    }
}
