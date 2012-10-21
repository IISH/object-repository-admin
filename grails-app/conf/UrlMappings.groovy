class UrlMappings {

    static mappings = {

        "/file/$bucket/$pid**"(controller: 'file', action:'file')
        "/metadata/$pid**"(controller: 'file', action: "metadata")

        "/"(controller: "dashboard")
        "/$na"(controller: "dashboard")
        "500"(view: '/error')
        "/login/$action?"(controller: "login")
        "/logout/$action?"(controller: "logout")

        "/$na/$controller/$action?/$id?" {
        }
    }
}
