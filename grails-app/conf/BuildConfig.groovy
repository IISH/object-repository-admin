grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
grails.offline.mode = false

grails.project.fork = [test: false]

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits('global') {
        excludes 'ehcache'
    }
    log 'error' // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenLocal()
        mavenRepo 'http://repo.spring.io/milestone'
        //mavenRepo 'http://snapshots.repository.codehaus.org'
        //mavenRepo 'http://repository.codehaus.org'
        //mavenRepo 'http://download.java.net/maven/2/'
        //mavenRepo 'http://repository.jboss.com/maven2/'
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // group, name, version
        compile 'org.apache.ftpserver:ftpserver-core:1.0.6'
        compile 'com.lowagie:itext:4.2.0'
        compile 'org.apache.activemq:activemq-camel:5.8.0'
        compile 'org.apache.activemq:activemq-pool:5.8.0'
    }

    plugins {
        runtime ':jquery:1.11.1'
        runtime ':resources:1.2.8'
        build ':tomcat:7.0.54'
        compile ':webxml:1.4.1'
        compile ':spring-security-core:2.0-RC2'
        compile ':spring-security-ldap:2.0-RC2'
        compile ':spring-security-oauth2-provider:1.0.5.2'
        compile ':routing:1.3.2'
        compile ':mail:1.0.6'
	    compile ':mongodb:3.0.1'
    }
}
