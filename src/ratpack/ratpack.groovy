import static ratpack.groovy.Groovy.*
import static ccserver.Dropbox.*
import static ccserver.Config.*
import com.dropbox.core.*
import ratpack.groovy.markup.internal.DefaultMarkup
import ratpack.form.Form


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
            render groovyTemplate('index.html', imagesMap: imagesMap)
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

        get('sync') {
            if (token) {
                render 'Aplicación sincronizada'
            } else {
                Map<String, String> config = configMap

                DbxWebAuthNoRedirect webAuth = getDropboxWebAuth(config.dropbox.appKey as String, config.dropbox.appSecret as String)
                String authorizeUrl = webAuth.start()

                render groovyTemplate('sync.html', authorizeUrl: authorizeUrl)
            }
        }

        post ('oauthBack') {
            Form form = parse(Form.class)

            Map<String, String> config = configMap
            DbxWebAuthNoRedirect webAuth = getDropboxWebAuth(config.dropbox.appKey as String, config.dropbox.appSecret as String)
            println "He llamado a dropbox: $webAuth"

            DbxAuthFinish authFinish = webAuth.finish(form.code)

            saveToken(authFinish.accessToken)

            render groovyTemplate('oauthBack.html')
        }

        get ('dropbox') {
            String directory = configMap.dropbox.directory
            def dropboxClient = getDropboxClient(token)
            def dbxFolder = dropboxClient.getMetadataWithChildren(directory)
            String accountName = dropboxClient.accountInfo.displayName
            
            render groovyTemplate('dropbox.html', accountName: accountName, directory: directory, dbxFolder: dbxFolder)
        }

        assets 'public'
 
   }
}