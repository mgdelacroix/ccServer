package ccserver

class Config {

    static Boolean saveToken(String token) {
        String homeDir = System.getProperty('user.home')
        File tokenFile = new File(homeDir, '.ccServer/token')

        if (tokenFile.exists()) tokenFile.delete()

        tokenFile << token
    }

    static String getToken() {
        String homeDir = System.getProperty('user.home')
        return new File(homeDir, '.ccServer/token').text
    }

    static Map<String, String> getConfigMap() {
        String homeDir = System.getProperty('user.home')
        File configFile = new File(homeDir, '.ccServer/Config.groovy')
        return new ConfigSlurper().parse(configFile.toURL())
    }

}