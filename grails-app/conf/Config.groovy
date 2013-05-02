grails.project.groupId = "org.objectrepository" // change this to alter the default package name and Maven publishing destination
grails.camel.camelContextId = "camelContext"
grails.views.javascript.library = "jquery"
['ldap', 'oauthProvider', 'plans', 'addUsers', 'ftp', 'screenLogin'].each {
    delegate."$it" = Boolean.parseBoolean(System.properties.getProperty(it, 'false'))
}

grails.plugins.springsecurity.ldap.active = ldap
grails.plugins.springsecurity.oauthProvider.active = oauthProvider
grails.plugins.springsecurity.oauthProvider.tokenServices.accessTokenValiditySeconds = 31536000
grails.plugins.springsecurity.oauthProvider.tokenServices.refreshTokenValiditySeconds = 31536000
grails.plugins.springsecurity.controllerAnnotations.staticRules = ['/oauth/authorize.dispatch': ['ROLE_OR_USER']]

updateList.interval = 60000 // setInterval in ms for the Javascript Ajax function

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/video-js/*', '/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.converters.default.pretty.print = true

// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

grails.config.locations = [ContentTypeConfig, PlanConfig]
if (System.properties.containsKey("or.properties")) {
    println("Loading properties from " + System.properties["or.properties"])
    grails.config.locations << "file:" + System.properties["or.properties"]
} else if (System.getenv()?.containsKey("OR")) {
    println("Loading properties from " + System.getenv().get("OR"))
    grails.config.locations << "file:" + System.getenv().get("OR")
} else {
    println("FATAL: no or.properties file set in VM or Environment. \n \
        Add a -Dor.properties=/path/to/or.properties argument when starting this application. \n \
        Or set a OR=/path/to/or.properties as environment variable.")
    //System.exit(-1)
}

// The access matrix has policies ( like 'closed', 'restricted' ) and each determines the access status of a bucket
accessStatus = ['open', 'restricted', 'closed']
accessMatrix = [
        closed: [
                [bucket: 'master', access: 'closed'],
                [bucket: 'level1', access: 'closed'],
                [bucket: 'level2', access: 'closed'],
                [bucket: 'level3', access: 'closed']
        ],
        restricted: [
                [bucket: 'master', access: 'closed'],
                [bucket: 'level1', access: 'restricted'],
                [bucket: 'level2', access: 'open'],
                [bucket: 'level3', access: 'open']
        ],
        open: [
                [bucket: 'master', access: 'closed'],
                [bucket: 'level1', access: 'open'],
                [bucket: 'level2', access: 'open'],
                [bucket: 'level3', access: 'open']
        ],
        deleted: [
                [bucket: 'master', access: 'closed'],
                [bucket: 'level1', access: 'closed'],
                [bucket: 'level2', access: 'closed'],
                [bucket: 'level3', access: 'closed']
        ]
]

// LDAP settings
def dn = ",dc=socialhistoryservices,dc=org"

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'org.objectrepository.security.User'
grails.plugins.springsecurity.authority.className = 'org.objectrepository.security.Role'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'org.objectrepository.security.UserRole'

grails.plugins.springsecurity.rememberMe.persistent = false
grails.plugins.springsecurity.rememberMe.useSecureCookie = false
grails.plugins.springsecurity.ldap.search.base = 'ou=users' + dn
grails.plugins.springsecurity.ldap.authorities.groupSearchBase = 'ou=groups' + dn
grails.plugins.springsecurity.ldap.rememberMe.usernameMapper.userDnBase = 'ou=users' + dn
grails.plugins.springsecurity.ldap.rememberMe.usernameMapper.usernameAttribute = 'cn' // must be set, e.g. 'cn'
grails.plugins.springsecurity.ldap.rememberMe.detailsManager.groupSearchBase = 'ou=groups' + dn
grails.plugins.springsecurity.ldap.mapper.userDetailsClass = 'LdapUser'
grails.plugins.springsecurity.ldap.authorities.retrieveGroupRoles = true
grails.plugins.springsecurity.ldap.authorities.retrieveDatabaseRoles = false
grails.plugins.springsecurity.ldap.groupAttribute = 'cn'
grails.plugins.springsecurity.ldap.useRememberMe = false

def serverPort = System.properties['server.port']
resolveBaseUrl = "http://localhost:${serverPort}/${appName}"

if (!screenLogin) grails.plugins.springsecurity.apf.filterProcessesUrl = "/" // This breakage is deliberate

grails.plugins.springsecurity.providerNames = ['daoAuthenticationProvider', 'anonymousAuthenticationProvider']
if (ldap) grails.plugins.springsecurity.providerNames.add(0, 'ldapAuthProvider')

environments {
    production {
        grails.plugins.springsecurity.successHandler.defaultTargetUrl = "/login"
        grails.logging.jul.usebridge = true
    }
    development {
        grails.serverURL = "http://localhost:${serverPort}/${appName}"
        grails.plugins.springsecurity.successHandler.defaultTargetUrl = "/${appName}/login"
        grails.logging.jul.usebridge = false
        grails.gorm.failOnError = true
    }
    test {
        grails.serverURL = "http://localhost:${serverPort}/${appName}"
        grails.plugins.springsecurity.workflow.active = false
        grails.plugins.springsecurity.ldap.active = false
        grails.plugins.springsecurity.oauthProvider.active = false
        grails.gorm.failOnError = true
    }
}

// log4j configuration
// We assume the production environment is running in an tomcat container. If not we use the application path's target folder.
final String catalinaBase = System.properties.getProperty('catalina.base', './target') + "/logs"
File logFile = new File(catalinaBase)
logFile.mkdirs()
println("log directory: " + logFile.absolutePath)

String loglevel = System.properties.getProperty('loglevel', 'warn')
log4j = {

    appenders {
        console name: 'StackTrace'
        rollingFile name: 'stacktrace', maxFileSize: 1024,
                file: logFile.absolutePath + '/objectrepository-admin-stacktrace.log'
    }

    root {
        "$loglevel"()
    }
    "$loglevel" 'grails.app.controllers'
    "$loglevel" 'grails.app.services'
}

grails.doc.title = "Object-repository"
//grails.doc.subtitle
grails.doc.authors = "Lucien van Wouw"
grails.doc.license = "Licensed under the Apache License, Version 2.0"
grails.doc.copyright = "Copyright (c) 2013 Social History Services"
//grails.doc.footer
//grails.doc.css
grails.doc.images = new File("src/docs/images")
