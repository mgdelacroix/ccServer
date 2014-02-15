@Grab("io.ratpack:ratpack-groovy:0.9.0")
import static ratpack.groovy.Groovy.*
import groovy.json.JsonBuilder

String json(Map mapa) {
    new JsonBuilder(mapa).toString()
}

ratpack {
    handlers {
        get {
            File file = new File('/tmp/file')
            response.send 'image/jpg', file.bytes
        }
        get('json') {
            Map<String, String> mapa = [hello: 'world!']
            response.send 'application/json', json(mapa)
        }
        get('html/:name?') {
            render htmlBuilder {
                head {
                    title 'Test html builder'
                }
                body {
                    h1 "Yo (${context.pathTokens.name ?: 'Anonymous'}) soy tu padre"
                    a (href: '/') {
                        img src: '/', width: '300px', height: '175px'
                    }
                    p 'en serio?'
                }
            }
        }
        get(":a") {
            render "working on $context.pathTokens.a, yo"
        }
    }
}