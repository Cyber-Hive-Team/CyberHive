## Architectural Error Handling Strategy

Our team chose **Strategy A – Functional Error Handling** using Kotlin `Result`.

We chose this strategy because it fits the way our project is organized into three main layers: **Data, Domain, and
Presentation**, while keeping each layer’s responsibility clear.

In the **Data layer**, some Repository operations use `runCatching` and return `Result` to handle possible errors, while
other operations use `nullable` or `Boolean` depending on the operation. In the **Domain layer**, Use Cases handle these
results when needed. In some parts of the **Presentation layer**, such as **GreedyFleetDispatcherRunner**, `Result` is
handled using `onSuccess` and `onFailure`.

We also use **Custom Domain Exceptions** such as `EntityValidationException`, `NetworkUnavailableException`, and
`DatabaseConflictException` to represent different types of errors.

We chose this approach because it fits the current architecture of the project and helps us handle errors in a simple
way without using excessive `try/catch` blocks, while keeping the responsibilities of the **Data, Domain, and
Presentation** layers clear.
