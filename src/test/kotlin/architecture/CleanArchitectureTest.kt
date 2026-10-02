package architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class CleanArchitectureTest {

    // -------------------------------------------------------------------------
    // Layer Isolation — single source of truth for dependency direction.
    // Domain must stay pure; Data and Presentation may depend on Domain only.
    // This single block replaces the separate manual import checks that used
    // to duplicate this rule (they never caught anything this block doesn't).
    // -------------------------------------------------------------------------

    @Test
    fun `layers respect Clean Architecture dependency direction`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val domain = Layer("Domain", "org.example.domain..")
                val data = Layer("Data", "org.example.data..")
                val presentation = Layer("Presentation", "org.example.presentation..")

                domain.dependsOnNothing()
                data.dependsOn(domain)
                presentation.dependsOn(domain)
            }
    }

    @Test
    fun `domain layer must not import third party libraries`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.domain..")
            .assertTrue {
                it.containingFile.imports.none { import ->
                    import.name.startsWith("io.ktor") ||
                            import.name.startsWith("kotlinx.serialization") ||
                            import.name.startsWith("kotlinx.coroutines") ||
                            import.name.startsWith("io.github.jan.supabase")
                }
            }
    }

    @Test
    fun `all classes in domain usecase must end with UseCase`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.domain.usecase..")
            .assertTrue { it.name.endsWith("UseCase") }
    }

    @Test
    fun `all classes in domain validator must end with Validator`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.domain.validator")
            .assertTrue { it.name.endsWith("Validator") }
    }

    @Test
    fun `every UseCase class must have a public operator fun invoke`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.domain.usecase..")
            .assertTrue {
                it.hasFunction { function ->
                    function.name == "invoke" &&
                            function.hasOperatorModifier
                }
            }
    }

    @Test
    fun `all DTO classes in data remote dto must end with Dto and be annotated with Serializable`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.data.remote.dto..")
            .assertTrue {
                it.name.endsWith("Dto") &&
                        it.hasAnnotationOf(kotlinx.serialization.Serializable::class)
            }
    }

    @Test
    fun `all interfaces in domain repository must end with Repository`() {
        Konsist
            .scopeFromProduction()
            .interfaces()
            .withPackage("org.example.domain.repository..")
            .assertTrue { it.name.endsWith("Repository") }
    }

    @Test
    fun `all classes in domain decorator must end with Decorator`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.domain.decorator..")
            .assertTrue { it.name.endsWith("Decorator") }
    }

    @Test
    fun `all classes in data repositoryImplementation must end with Impl`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.data.repositoryImplementation..")
            .filter { it.name != "BaseRepository" }
            .assertTrue { it.name.endsWith("Impl") }
    }

    @Test
    fun `all repository implementations must implement a domain repository interface`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.data.repositoryImplementation..")
            .filter { it.name.endsWith("Impl") }
            .assertTrue {
                it.hasParent { parent ->
                    parent.name.endsWith("Repository")
                }
            }
    }

    @Test
    fun `all supabase datasource classes must implement a remote datasource interface`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.data.datasource.remote.supabase..")
            .assertTrue {
                it.hasParent { parent ->
                    parent.name.endsWith("DataSource")
                }
            }
    }

    @Test
    fun `data datasource must not import from domain usecase`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.data.datasource..")
            .assertTrue {
                it.containingFile.imports.none { import ->
                    import.name.startsWith("org.example.domain.usecase")
                }
            }
    }

    @Test
    fun `data remote dto must not import from domain`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.data.remote.dto..")
            .assertTrue {
                it.containingFile.imports.none { import ->
                    import.name.startsWith("org.example.domain")
                }
            }
    }

    // NOTE: TraceHubLineageUseCase currently fails this test.
    // Current issue: It receives WarehouseNode (domain model) as constructor dependency.
    // Required fix: Move the tree parameter to invoke() as input, and inject WarehouseRepository instead.
    // Example:
    //   class TraceHubLineageUseCase(private val warehouseRepository: WarehouseRepository) {
    //       operator fun invoke(tree: WarehouseNode, warehouseId: String): List<WarehouseNode>
    //   }
    @Test
    fun `every UseCase with constructor parameters must depend on a Repository`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage("org.example.domain.usecase..")
            .filter { it.primaryConstructor?.parameters?.isNotEmpty() == true }
            .assertTrue {
                it.primaryConstructor?.parameters?.any { param ->
                    param.type.name.endsWith("Repository")
                } ?: false
            }
    }
}