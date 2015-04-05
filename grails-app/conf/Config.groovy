grails.project.groupId = "org.objectrepository" // change this to alter the default package name and Maven publishing destination
grails.camel.camelContextId = "camelContext"
grails.views.javascript.library = "jquery"

['ldap', 'oauthProvider', 'plans', 'addUsers', 'ftp', 'screenLogin'].each {
    delegate."$it" = Boolean.parseBoolean(System.properties.getProperty(it, 'false'))
}

updateList.interval = 60000 // setInterval in ms for the Javascript Ajax function

grails.mime.file.extensions = false // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = true
grails.mime.types = [html         : ['text/html', 'application/xhtml+xml'],
                     xml          : ['text/xml', 'application/xml'],
                     text         : 'text/plain',
                     js           : 'text/javascript',
                     rss          : 'application/rss+xml',
                     atom         : 'application/atom+xml',
                     css          : 'text/css',
                     csv          : 'text/csv',
                     all          : '*/*',
                     json         : ['application/json', 'text/json'],
                     form         : 'application/x-www-form-urlencoded',
                     multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/video-js/*', '/images/*', '/css/*', '/js/*', '/plugin/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64
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
grails.exceptionresolver.params.exclude = ['password', 'authorization', 'client_secret']

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
// Definitions accessStatus taken from:
// http://purl.org/dc/terms/available
buckets = ['master', 'level1', 'level2', 'level3']
accessStatus = ['open', 'restricted', 'closed']
accessMatrix = [
        closed    : [
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
        open      : [
                [bucket: 'master', access: 'closed'],
                [bucket: 'level1', access: 'open'],
                [bucket: 'level2', access: 'open'],
                [bucket: 'level3', access: 'open']
        ]
]

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.rememberMe.persistent = false
grails.plugin.springsecurity.rememberMe.useSecureCookie = false
grails.plugin.springsecurity.userLookup.userDomainClassName = 'org.objectrepository.security.User'
grails.plugin.springsecurity.authority.className = 'org.objectrepository.security.Role'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'org.objectrepository.security.UserRole'

// Set the authentication providers
grails.plugin.springsecurity.providerNames = ['daoAuthenticationProvider', 'anonymousAuthenticationProvider']
if (ldap) grails.plugin.springsecurity.providerNames.add(0, 'ldapAuthProvider')
if (oauthProvider) grails.plugin.springsecurity.providerNames.add(0, 'clientCredentialsAuthenticationProvider')
print('providernames=')
println(grails.plugin.springsecurity.providerNames)
grails.plugin.springsecurity.ldap.active = ldap
grails.plugin.springsecurity.ldap.authorities.retrieveGroupRoles = true
grails.plugin.springsecurity.ldap.authorities.retrieveDatabaseRoles = false
grails.plugin.springsecurity.ldap.groupAttribute = cn
grails.plugin.springsecurity.ldap.useRememberMe = false
grails.plugin.springsecurity.rememberMe.persistent = false
grails.plugin.springsecurity.rememberMe.useSecureCookie = false
grails.plugin.springsecurity.oauthProvider.active = oauthProvider
grails.plugin.springsecurity.oauthProvider.tokenServices.accessTokenValiditySeconds = 31536000
grails.plugin.springsecurity.oauthProvider.tokenServices.refreshTokenValiditySeconds = 31536000
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        '/oauth/authorize.dispatch': ["isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"],
        '/oauth/token.dispatch'    : ["isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"]
]

grails.plugin.springsecurity.filterChain.chainMap = [
        '/oauth/token'              : 'JOINED_FILTERS,-oauth2ProviderFilter,-securityContextPersistenceFilter,-logoutFilter,-exceptionTranslationFilter',
        '/*/dashboard': 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-exceptionTranslationFilter',
        '/**'                       : 'JOINED_FILTERS,-statelessSecurityContextPersistenceFilter,-oauth2ProviderFilter,-clientCredentialsTokenEndpointFilter,-oauth2ExceptionTranslationFilter'
]


oauthclient = [
        'client_1': [
                authorizedGrantTypes:
                        ['authorization_code', 'refresh_token', 'implicit', 'password', 'client_credentials'],
                authorities         :
                        ['OR_USER'],
                scopes              :
                        ['read', 'write'],
                redirectUris        :
                        ['http://localhost']
        ]
]


def serverPort = System.properties['server.port']
serverURL = "http://localhost:${serverPort}/${appName}"

if (!screenLogin) grails.plugin.springsecurity.apf.filterProcessesUrl = "/" // This breakage is deliberate

environments {
    production {
        grails.plugin.springsecurity.successHandler.defaultTargetUrl = "/login"
        grails.logging.jul.usebridge = true
    }
    development {
        grails.serverURL = resolveBaseUrl
        grails.plugin.springsecurity.successHandler.defaultTargetUrl = "/${appName}/login"
        grails.logging.jul.usebridge = false
        grails.gorm.failOnError = true
        //grails.plugin.springsecurity.debug.useFilter = true
    }
    test {
        grails.serverURL = serverURL
        grails.plugin.springsecurity.workflow.active = false
        grails.plugin.springsecurity.ldap.active = false
        grails.plugin.springsecurity.oauthProvider.active = false
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
    "$loglevel" 'grails.plugin.springsecurity.web.filter.DebugFilter'
}

grails.doc.title = "Object repository"
grails.doc.subtitle = "Object repository"
grails.doc.authors = "Lucien van Wouw"
grails.doc.license = "Licensed under the Apache License, Version 2.0"
grails.doc.copyright = "Copyright (c) 2014 Social History Services"
//grails.doc.footer
//grails.doc.css
grails.doc.images = new File("src/docs/images")

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}

// Added by the Spring Security OAuth2 Provider plugin:
grails.plugin.springsecurity.oauthProvider.clientLookup.className = 'org.objectrepository.security.OrOAuth2Client'
grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.className = 'org.objectrepository.security.AuthorizationCode'
grails.plugin.springsecurity.oauthProvider.accessTokenLookup.className = 'org.objectrepository.security.AccessToken'
grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.className = 'org.objectrepository.security.RefreshToken'

