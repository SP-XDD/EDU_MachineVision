/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Start class for the application
 */

package dev.namexdd.machinevision_hw

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import org.opencv.core.Core

class MachineVisionApplication : Application() {

    companion object {
        var primaryStage: Stage? = null

        // Size for application window (main frame)
        const val WINDOW_WIDTH = 500.0
        const val WINDOW_HEIGHT = 300.0
        const val WINDOW_TITLE = "Machine Vision"
    }

    override fun start(stage: Stage) {
        // Assign primary stage
        primaryStage = stage

        // Load FXML for main window
        val fxmlLoader = FXMLLoader(MachineVisionApplication::class.java.getResource("main-window-view.fxml"))
        val scene = Scene(fxmlLoader.load(), WINDOW_WIDTH, WINDOW_HEIGHT)

        // Configure stage
        stage.title = WINDOW_TITLE
        stage.icons.add(Image(MachineVisionApplication::class.java.getResourceAsStream("/dev/namexdd/machinevision_hw/images/appicon.png")))
        stage.scene = scene
        stage.isResizable = false
        stage.show()
    }
}

fun main() {
    // Load OpenCV native library
    try {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        println("[MV] OpenCV loaded successfully!")
    } catch (e: UnsatisfiedLinkError) {
        e.printStackTrace()
        println("[MV] Error loading OpenCV library!")
        return
    }

    // Launch JavaFX application
    Application.launch(MachineVisionApplication::class.java)
}