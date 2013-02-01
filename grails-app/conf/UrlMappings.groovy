class UrlMappings {

    static mappings = {

        "/file/$bucket/$pid**"(controller: 'file', action: 'file')
        "/metadata/$pid**"(controller: 'file', action: "metadata")
        "/viewer/$pid**"(controller: 'viewer', action: 'index')
        "/mets/$na/$label?"(controller: 'mets', action: 'index')

        "/"(controller: "dashboard")
        "500"(view: '/error')
        "/login/$action?"(controller: "login")
        "/logout/$action?"(controller: "logout")

        "/$controller/$action?/$id?" {
        }
    }
}
