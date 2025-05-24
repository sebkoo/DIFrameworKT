object DIFramework {
    // Part 1 - create two new annotations

    // Layer  - runtime annotation, can be attached to classes and interfaces ...
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Layer

    // Inject - runtime annotation, can be attached to PROPERTIES only
    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Inject

    @Layer  // data layer
    class Repository {
        fun getData(): String {
            return "data from repository"
        }
    }

    @Layer  // business logic
    class Service {
        @Inject
        lateinit var repository: Repository

        fun performAction(): String {
            return repository.getData() + " - with some business logic"
        }
    }

    @Layer
    class UserManager {
        private val loggedUsers = mutableSetOf<String>()
        fun login(userName: String) {
            loggedUsers.add(userName)
            println("[log] Logged in as $userName")
        }

        fun isLoggedIn(userName: String) =
            userName in loggedUsers

        fun logout(userName: String) {
            loggedUsers.remove(userName)
            println("[log] $userName just logged out")
        }
    }

    @Layer  // HTTP requests
    class Controller {
        @Inject
        lateinit var service: Service
        @Inject
        lateinit var users: UserManager

        fun processHTTPRequest(payload: String, userName: String = "invalid@jvm.com"): String {
            return if (users.isLoggedIn(userName))
                "Processed request! Response: ${service.performAction()}"
            else "Not logged in, request denied"
        }
    }

    // Controller (HTTP) - Service - Repository
    //                  \- UserManager
}