class UrlMappings {

    static mappings = {

        "/file/$bucket/$pid**"(controller: 'file', action:'file')
        "/metadata/$bucket/$pid**"(controller: 'file', action: "metadata")

        "/"(controller: "dashboard")
        "500"(view: '/error')
        "/login/$action?"(controller: "login")
        "/logout/$action?"(controller: "logout")

        "/$controller/$action?/$id?" {
        }
    }
}
