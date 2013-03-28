class UrlMappings {

    static mappings = {

        "/file/$bucket/$pid**"(controller: 'file', action: 'file')
        "/metadata/$pid**"(controller: 'file', action: "metadata")
        "/viewer/$pid**"(controller: 'viewer', action: 'index')
        "/mets/$na/$objid?"(controller: 'mets', action: 'index')
        "/pdf/$na/$objid/$bucket?"(controller: 'pdf', action: 'index')

        "/login/$action?"(controller: "login")
        "/logout/$action?"(controller: "logout")
        "/"(controller: "login")

        name namingAuthority: "/$na/$controller/$action?/$id?"()

        "500"(view: '/error')
    }
}
