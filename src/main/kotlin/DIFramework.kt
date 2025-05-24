import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.createInstance

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

    class DIManager {
        private val layers= mutableMapOf<KClass<*>, Any>()

        // Part 2 - add a function which registers a class into the layers map
        fun <T: Any> register(clazz: KClass<T>): Unit {
            // if clazz has the annotation @Layer, instantiate the class and add the association
            // clazz -> that instance
            if (clazz.findAnnotation<Layer>() != null) {
                val instance = clazz.createInstance()
                layers[clazz] = instance
            }
        }
}