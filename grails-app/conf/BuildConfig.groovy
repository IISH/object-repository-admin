grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.offline.mode = false

//grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false

    repositories {
        grailsRepo 'http://grails.org/plugins'
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenLocal()
        mavenRepo 'http://repo.grails.org/grails/plugins'
        mavenRepo 'http://maven.springframework.org/milestone/'
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // group, name, version
        compile "org.apache.ftpserver:ftpserver-core:1.0.6"
        compile "com.lowagie:itext:2.1.7"
        compile "org.apache.activemq:activemq-camel:5.8.0"
        compile "org.apache.activemq:activemq-pool:5.8.0"
    }

    plugins {
        runtime ":jquery:1.8.3"
        runtime ":resources:1.1.6"
        build ":tomcat:$grailsVersion"
        compile ":webxml:1.4.1"
        compile ":spring-security-core:1.2.7.3"
        compile ":spring-security-ldap:1.0.6"
        compile ":spring-security-oauth2-provider:1.0.0.M5.1"
        compile ":mongodb:1.1.0.GA"
        compile ":mail:1.0.1"
        compile ":routing:1.2.2"
    }
}