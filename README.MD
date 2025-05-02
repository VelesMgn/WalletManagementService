<a id="anchor"></a>
# WalletManagementService
____
_Stack_:
Java 17, Spring Boot, Maven, PostgreSQL, Hibernate,
Lombok, Slf4j, Liquibase, JUnit, Docker
____

__WalletManagementService__ - это backend-сервис для управления кошельками, построенный на Java и Spring Boot.  
Сервис реализует основные функции работы с электронными кошельками:

- Создание кошелька
- Пополнение и списание средств
- Просмотр состояния и баланса кошелька
- Удаление кошелька
- Поддержка конкурентных операций


  Вся логика надежно интегрирована с PostgreSQL, 
  а миграции данных выполняются с помощью Liquibase. 
  Корректная обработка ошибок — если запрос некорректен 
  или средств недостаточно, 
  система вернет информативный и структурированный ответ.

### REST API:

* POST /api/v1/wallets/operations - Проведение операции (пополнение или списание):    
Пример тела запроса:
```  
  {
    walletId: UUID,
    operationType: DEPOSIT or WITHDRAW,
    amount: 1000
  }
```
* GET `/api/v1/wallets/{walletId}` - Получение данных и баланса конкретного кошелька.
* POST `/api/v1/wallets/create` - Создание нового кошелька.
* GET `/api/v1/wallets` - Получение информации обо всех существующих кошельках.
* DELETE `/api/v1/wallets/{walletId}` - Удаление кошелька по UUID.

### Руководство по запуску:

Перед началом убедитесь, что на вашем компьютере установлены:

* [Git]( https://git-scm.com/downloads).

* [Docker Desktop (или Docker Engine и Docker Compose)](https://www.docker.com/products/docker-desktop/).

Склонируйте репозиторий:

* cd <название_директории_проекта>
* git clone <ссылка_на_репозиторий>

Сконфигурируйте переменные окружения `.env`

Соберите приложение: mvn clean package

Постройте и запустите контейнеры:

docker compose up --build

[вверх](#anchor)