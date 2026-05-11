#Сервис подбора продуктов/подписок

---

##Переменные окружения

| ENV name      | Description           | Default value         |
| :---          |    :----              |              :---     |
|PRODCAT_PRC_DB_URL | ссылка на БД продката |jdbc:postgresql://postgres.it-alnc.ru:5432/prodcat_alfa_dev|
|PRODCAT_PRC_DB_USER| пользователь БД       |prc                    |
|PRODCAT_PRC_DB_PASSWORD| пароль БД         |prc                    |
|PRODCAT_PRC_DB_SCHEMA      | схема БД              |PRC                    |
|PRODCAT_PRC_DB_DRIVER      | драйвер БД              |org.postgresql.Driver                    |
|ITA_INFO| ссылка на ita-info     |http://192.168.0.18:8071 |
|KAFKA_SERVERS  | ссылка на сервер(ы) kafka|itaforms-node1.it-alnc.ru:9092,itaforms-node2.it-alnc.ru:9092|
|PACKAGESEARCH_LOG_LEVEL      | уровень логирования   |info                   |
|PACKAGESEARCH_LOGS_PATH      | путь к файлу логов    |/logs/packagesearchservice.log|
