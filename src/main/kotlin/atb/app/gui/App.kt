package atb.app.gui

import javafx.application.Application


class JFXApp : tornadofx.App(ATBMainView::class)

fun main(args: Array<String>) {
    Application.launch(JFXApp::class.java, *args)
}