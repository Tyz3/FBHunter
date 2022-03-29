# FBHunter

Сбор информации по пользователям facebook

# Установка и Запуск

1. Положить рядом с .jar файлом актуальную версию [chromedriver](https://chromedriver.storage.googleapis.com/index.html) (должна совпадать с версией браузера у вас не устройстве)
2. Установить [Java 16](https://www.oracle.com/java/technologies/javase/jdk16-archive-downloads.html)
3. Запустить FBHunter.jar из PowerShell или CMD
```
java -jar FBHunter.jar
```

# API

**GET**

1. **/getTaskInfo?taskId=**
```
код: 200
{
  "id": x,
  "type": тип задачи,
  "status": статус задачи,
  "result": x,
  "login": FB логин,
  "password": FB пароль,
  "proxy": используемое прокси,
  "startTime": время создания задачи,
  "endTime": время завершения задачи
}
```

**POST**

1. **/collectAboutInfo**

body: {login=, password=, targetIds=[], proxy=}

Сбор информации по пользователям, указанных в параметре targetIds

Ответ JSON дополняется ответом result
```
{
  ...
  "result": [
    {
      "targetId": FB id пользователя,
      "phoneNumbers": [номера телефонов, найденные на странице],
      "emails": [почтовые адреса, найденные на странице],
      "sites": [ссылки на сайты, найденные на странице],
      "gender": пол,
      "residenceCity": город проживания,
      "nativeCity": родной город,
      "userName": имя пользователя,
      "mainPhotoUrl": фото в base64
    },
    ...
  ],
  ...
}
```
2. **/collectFriendsIds**

body: {login=, password=, targetId=, maxAmount=, proxy=}

Ответ JSON дополняется ответом result
```
{
  ...
  "result": {
    "targetId": FB id пользователя,
    "friendsIds": [список друзей пользователя]
  },
  ...
}
```

# Пример выходных данных
**/collectAboutInfo**
```
  "result": [
    ... 98 ед.
    {
      "targetId": "janethgc9",
      "gender": "Женский",
      "residenceCity": "Пилар",
      "nativeCity": "Лима",
      "userName": "Julie Janet Gutierrez",
      "phoneNumbers": [],
      "mainPhotoUrl": "base64"
    },
    {
      "targetId": "joanna.dibble.1",
      "userName": "JoAnna Dibble",
      "mainPhotoUrl": "base64"
    }
  ],
  "proxy": "socks5://193.23.50.55:10810",
  "password": "***",
  "startTime": "31.01.2022 23:28:12",
  "id": 10,
  "endTime": "01.02.2022 00:57:16",
  "type": "AboutInfoTask",
  "login": "***@***.***",
  "status": "DONE"
}
```
