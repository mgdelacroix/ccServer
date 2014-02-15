@Grab("io.ratpack:ratpack-groovy:0.9.0")
import static ratpack.groovy.Groovy.*

String contentType() {
    'image/jpg'
}

ratpack {
    handlers {
        get {
            File file = new File('/tmp/file')
            response.send contentType(), file.bytes
        }
        get(":a") {
            render "working on $context.pathTokens.a, yo"
        }
    }
}