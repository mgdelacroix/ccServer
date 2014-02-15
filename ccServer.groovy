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
            response.send contentType(), file.bytes
        }
        get('json') {
            Map<String, String> mapa = [hola: 'mundo']
            response.send 'application/json', json(mapa)
        }
        get('html') {
            render htmlBuilder {
                head {
                    title 'Test html builder'
                }
                body {
                    h1 'Yo soy tu padre'
                    p 'en serio?'
                }
            }
        }
        get(":a") {
            render "working on $context.pathTokens.a, yo"
        }
    }
}