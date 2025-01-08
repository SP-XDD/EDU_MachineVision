/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Navigator is a utility class that provides a method for changing the scene to the main menu
 */

package dev.namexdd.machinevision_hw.utils

import dev.namexdd.machinevision_hw.MachineVisionApplication
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene

object Navigator {
    fun goBack() {
        try {
            val stage = MachineVisionApplication.primaryStage
                ?: throw IllegalStateException("Primary stage is not initialized")

            //Main windows loader
            val resource = Navigator::class.java.getResource("/dev/namexdd/machinevision_hw/main-window-view.fxml")
                ?: throw IllegalStateException("main-window-view.fxml not found")

            val fxmlLoader = FXMLLoader(resource)
            val root = fxmlLoader.load<Parent>()

            // New scene parameters
            stage.scene = Scene(root, MachineVisionApplication.WINDOW_WIDTH, MachineVisionApplication.WINDOW_HEIGHT)
            stage.title = MachineVisionApplication.WINDOW_TITLE
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error changing scene to Main Menu")
        }
    }
}