package atb.app.gui

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import java.util.concurrent.ForkJoinPool


class JFXApp : tornadofx.App(ATBMainView::class) {
    override fun start(stage: Stage) {
        stage.setOnHidden {
            ForkJoinPool.commonPool().shutdownNow()
            Platform.exit()
            //System.exit(0)
        }
        super.start(stage)
    }
}

fun main(args: Array<String>) {
    Application.launch(JFXApp::class.java, *args)
}