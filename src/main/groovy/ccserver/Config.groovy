package ccserver

class Config {

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

}