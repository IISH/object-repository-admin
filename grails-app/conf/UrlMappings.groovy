class UrlMappings {

    static mappings = {

        "/file/$bucket/$pid**"(controller: 'file', action: 'file')
        "/iso9660/$bucket/$pid**"(controller: 'file', action: 'iso9660')
        "/metadata/$pid**"(controller: 'file', action: "metadata")
        "/viewer/$pid**"(controller: 'viewer', action: 'index')
        "/mets/$na/$objid?"(controller: 'mets', action: 'index')
        "/pdf/$na/$objid/$bucket?"(controller: 'pdf', action: 'index')
        "/$na/orfile/$action/$bucket/$pid**"(controller: 'orfile')

        "/login/$action?"(controller: "login")
        "/logout/$action?"(controller: "logout")
        "/lostpassword/$action?"(controller: "lostpassword")
        "/"(controller: "login")

        name namingAuthority: "/$na/$controller/$action?/$id?"()

        "500"(view: '/error')
    }
}
