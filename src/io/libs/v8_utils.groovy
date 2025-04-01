package io.libs

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Random
import java.util.Arrays
import java.util.stream.Collectors
import org.apache.commons.lang.RandomStringUtils

def getWorkspaceLine(workspace = "") {
    return workspace.isEmpty() ? "" : "cd ${workspace} &"
}

//выполнение команды системы
def cmd(command, workDir = "") {
    
    if (!workDir.isEmpty()) {
        command = "${getWorkspaceLine(workDir)} ${command}"
    }

    def returnCode = 0
    if (isUnix()) {
        returnCode = sh script: "${command}", returnStatus: true
    } else {
        returnCode = bat script: "chcp 65001\n ${command}", returnStatus: true
    }
    return returnCode
}

// Собирает основную конфигурацию из исходников
def buildCF(dir = '', uccode = ''){
    if (dir == '') {
        dir = env.WORKSPACE    
    }

    int result = 0
    def log_file = "${env.WORKSPACE}\\log.txt"
    returnCode = cmd("vrunner compile --src \"${dir}\\src\\cf\" -c --ibconnection /S${server1c}/${database} --db-user \"Admin\" --db-pwd \"123\" --v8version \"8.3.26.1521\" --uccode \"${uccode}\" ")
    
    if(returnCode > 0){
        error 'Исходники не собрались:\n' + loadErrorMessage()
    }
    return returnCode 
}

//обновление информационной базы
def updatedb(uccode = ''){
            
    returnCode = cmd("vrunner updatedb --v1 --ibconnection /S${server1c}/${database} --db-user \"Admin\" --db-pwd \"123\" --v8version \"8.3.26.1521\" --uccode \"${uccode}\" ")

    if (returnCode != 0) {
        error 'Ошибка при удалении базы:' 
    }
    return returnCode  
}

//тестирование подключения библиотеки
def hello_world(){
    echo('Hello, world!')
}

//синхронизация хранилища конфигурации 1с и гита
def sync_hran(rep_1c, rep_git_local, rep_git_remote, ext = "", aditional_parameters, server1c){
    start_sync = "gitsync sync --storage-user \"gitbot\" --storage-pwd \"demo\" ${ext} ${aditional_parameters} \"${rep_1c}\" \"${rep_git_local}\"";
    return cmd(start_sync);
}

//инициализация хранилища
def init_hran(rep_1c, rep_git_local, ext = "", server1c){
    init_sync = "gitsync init --storage-user \"gitbot\" --storage-pwd \"demo\" ${ext} \"${rep_1c}\" \"${rep_git_local}\"";
    return cmd(init_sync);
}

//отправляем собщение в тестовый чат телеги
def telegram_send_message(TOKEN,CHAT_ID, messageText,success){
    
    
    def icons = ["🛀","🚧", "😸", "🚀", "⌛", "🐟", "💪", "📀", "📷", "🐄", "🐈"] 
 
    def randomIndex = (new Random()).nextInt(icons.size())
    def randomIndex_message = (new Random()).nextInt(message_failure.size())
    
    messageText = escapeStringForMarkdownV2(messageText)

    if (success == true) {
                        messageText = escapeStringForMarkdownV2(messageText)
    messageText = "✅✅✅ ${messageText} Ссылка на сборку: ${env.BUILD_URL}" 
                    }else{ 
    
    messageText = "❌❌❌ ${messageText} Ссылка на сборку: ${env.BUILD_URL}"
    }

    sh """                  curl -s -X POST https://api.telegram.org/bot${TOKEN}/sendMessage \
                            -H "Content-type: application/x-www-form-urlencoded; charset=utf-8" \
                            -d chat_id=${CHAT_ID} \
                            -d text="${messageText}"
                            """

}

//служебный раздел
private static String escapeStringForMarkdownV2(String incoming) {

return incoming.replace('_', '\\_')

.replace('*', '\\*')

.replace('[', '\\[')

.replace(']', '\\]')

.replace('(', '\\(')

.replace(')', '\\)')

.replace('~', '\\~')

.replace('`', '\\`')

.replace('>', '\\>')

.replace('#', '\\#')

.replace('+', '\\+')

.replace('-', '\\-')

.replace('=', '\\=')

.replace('|', '\\|')

.replace('{', '\\{')

.replace('}', '\\}')

}
  