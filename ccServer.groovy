@Grab("io.ratpack:ratpack-groovy:0.9.0")
import static ratpack.groovy.Groovy.*
import ratpack.groovy.markup.internal.DefaultMarkup

String removePublicFromFile(File file) {
    return file.toString().split('/')[1..-1].join('/')
}

Map<String, String> getImagesMap() {
    File imagesDirectory = new File('public/images')

    Map<String, String> result = [:]
    imagesDirectory.eachFile { file ->
        result[file.name] = removePublicFromFile(file)
    }

    return result
}

DefaultMarkup getNonexistentImage() {
    htmlBuilder {
        head {
            title 'Obras de Laura Pareja'
        }
        body {
            h1 'La imagen solicitada no existe'
            p {
                a href: '/', 'Volver al índice'
            }
        }
    }
}

DefaultMarkup getImage(File image) {
    String fileName = image.name
    String url = '/' + removePublicFromFile(image)

    def license = {
        center {
            a (rel: "license", href: "http://creativecommons.org/licenses/by-nc-nd/4.0/deed.es_CO") {
                img alt: "Licencia Creative Commons", style: "border-width:0", src: "http://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png"
            }
            div(style: 'margin-top: 10px') {
                span 'xmlns:dct': "http://purl.org/dc/terms/", href: "http://purl.org/dc/dcmitype/StillImage", property: "dct:title", rel: "dct:type", 'TITULO-OBRA '
                text 'por'
                a 'xmlns:cc': "http://creativecommons.org/ns#", href: "http://ATRIBUIR-OBRA", property: "cc:attributionName", rel: "cc:attributionURL", 'ATRIBUIR-OBRA'
                text 'se distribuye bajo una'
                a rel: "license", href: "http://creativecommons.org/licenses/by-nc-nd/4.0/deed.es_CO", 'Licencia Creative Commons Atribución-NoComercial-SinDerivar 4.0 Internacional.'
            }
            div {
                text 'Basada en una obra en'
                a 'xmlns:dct': "http://purl.org/dc/terms/", href: "http://URL", rel: "dct:source", 'http://URL'
            }
        }
    }

    htmlBuilder {
        head {
            title "Obra: $fileName"
        }
        body {
            h1 'Obra'
            h2 fileName
            a (href: url) {
                img src: url, height: '75%'
            }
            div style: 'width: 60%', license
            p { a href: '/', 'Volver atrás' }
        }
    }
}

ratpack {
    handlers {
        get {
            render htmlBuilder {
                head {
                    title 'Obras de Laura Pareja'
                }
                body {
                    h1 'Obras de Laura Pareja'
                    p 'Lista de las obras'
                    ul {
                        imagesMap.each { name, url ->
                            li {
                                a (href: "obra/$name", name)
                            }
                        }
                    }
                }
            }
        }

        get('obra/:filename') {
            String filename = context.pathTokens.filename

            File file = new File("public/images/${filename}")

            if (file.exists()) {
                render getImage(file)
            } else {
                render nonexistentImage
            }
        }

        assets 'public'
    }
}