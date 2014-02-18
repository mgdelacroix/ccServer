import static ratpack.groovy.Groovy.*
import ratpack.groovy.markup.internal.DefaultMarkup
import com.dropbox.core.*

DbxClient getDropboxClient() {
    DbxClient client = new DbxClient(dropboxConfig, token)
}

DbxRequestConfig getDropboxConfig() {
    new DbxRequestConfig("ccServer", new Locale('es') as String)
}

DbxWebAuthNoRedirect getDropboxWebAuth(String appKey, String appSecret) {
    DbxAppInfo appInfo = new DbxAppInfo(appKey, appSecret)
    return new DbxWebAuthNoRedirect(dropboxConfig, appInfo)
}

Boolean saveToken(String token) {
    String homeDir = System.getProperty('user.home')
    File tokenFile = new File(homeDir, '.ccServer/token')

    if (tokenFile.exists()) tokenFile.delete()

    tokenFile << token
}

String getToken() {
    String homeDir = System.getProperty('user.home')
    return new File(homeDir, '.ccServer/token').text
}

Map<String, String> getConfigMap() {
    String homeDir = System.getProperty('user.home')
    File configFile = new File(homeDir, '.ccServer/Config.groovy')
    return new ConfigSlurper().parse(configFile.toURL())
}

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
                    code new groovy.json.JsonBuilder(configMap).toPrettyString()
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

        get('sync') {
            Map<String, String> config = configMap
            DbxWebAuthNoRedirect webAuth = getDropboxWebAuth(config.dropbox.appKey as String, config.dropbox.appSecret as String)

            String authorizeUrl = webAuth.start()

            render htmlBuilder {
                head {
                    title 'Dropbox Sync'
                }
                body {
                    h1 'Sincronización con Dropbox'
                    ol {
                        li {
                            span {
                                text "Por favor, dirígete a"
                                a href: authorizeUrl, target: '_blank', 'esta dirección'
                            }
                        }
                        li 'Copia el código de autorización'
                        li 'Introdúcelo en el campo que hay a continuación'
                    }
                    form (method: 'post', action: '/oauthBack') {
                        input type: 'text', name: 'code', placeholder: 'código'
                        input type: 'submit', value: 'Autenticar con Dropbox'
                    }
                }
            }
        }

        post ('oauthBack') {
            String code = request.text.split('=').last()

            Map<String, String> config = configMap
            DbxWebAuthNoRedirect webAuth = getDropboxWebAuth(config.dropbox.appKey as String, config.dropbox.appSecret as String)

            DbxAuthFinish authFinish = webAuth.finish(code)
            saveToken(authFinish.accessToken)

            render token
        }

        get ('dropbox') {
            render htmlBuilder {
                body {
                    h1 "Linked account: $dropboxClient.accountInfo.displayName"
                    ul {
                        dropboxClient.getMetadataWithChildren(configMap.dropbox.directory).children.each { child ->
                            li "$child.name ==> $child"
                        }
                    }
                }
            }
        }

        assets 'public'
 
   }
}