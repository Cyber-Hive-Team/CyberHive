package edu.bytebloom.logiroute.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cyberhive.frontend.logiroute.generated.resources.Res
import cyberhive.frontend.logiroute.generated.resources.byteloom_logo
import edu.logiroute.logiroute.App
import org.jetbrains.compose.resources.painterResource

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "ByteBloom LogiRoute",
        resizable = true,
        icon = painterResource(Res.drawable.byteloom_logo)
    ) {
        App()
    }
}