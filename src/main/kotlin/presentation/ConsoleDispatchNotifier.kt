package org.example.presentation

import org.example.domain.dispatch.DispatchNotifier

class ConsoleDispatchNotifier : DispatchNotifier {

    override fun notify(message: String) {
        println(message)
    }
}
