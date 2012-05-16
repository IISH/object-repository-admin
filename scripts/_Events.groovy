
eventCompileStart = { msg ->
    String hash = ("git rev-parse HEAD".execute().text)
    String href = '<a target="_blank" href="https://github.com/IISH/object-repository-admin/commit/"' + hash + '">github.com</a>"'
    new File("grails-app/views/_git.gsp").text = href
}