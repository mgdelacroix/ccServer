package ccserver

import com.dropbox.core.DbxClient
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.DbxWebAuthNoRedirect
import com.dropbox.core.DbxAppInfo

class Dropbox {

    static DbxClient getDropboxClient(String token) {
        DbxClient client = new DbxClient(dropboxConfig, token)
    }

    static DbxRequestConfig getDropboxConfig() {
        new DbxRequestConfig("ccServer", new Locale('es') as String)
    }

    static DbxWebAuthNoRedirect getDropboxWebAuth(String appKey, String appSecret) {
        DbxAppInfo appInfo = new DbxAppInfo(appKey, appSecret)
        return new DbxWebAuthNoRedirect(dropboxConfig, appInfo)
    }

}