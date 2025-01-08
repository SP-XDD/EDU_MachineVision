/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: MainController is a controller class for the main window of the application
 */

package dev.namexdd.machinevision_hw

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.control.Label

class MainController {

    lateinit var descriptionLabel: Label

    @FXML
    private fun onButtonClickColors() {
        changeScene("colors_thresholding-window-view.fxml", "Colors Thresholding")
    }

    @FXML
    private fun onButtonClickMObjects() {
        changeScene("multiple_objects-window-view.fxml", "Multiple Objects View")
    }

    @FXML
    private fun onButtonClickBarcode() {
        changeScene("barcode_reader-window-view.fxml", "Barcode View")
    }

    private fun changeScene(fxmlFile: String, title: String) {
        try {
            val stage = getCurrentStage()

            // New FXML loader
            val fxmlLoader = FXMLLoader(MachineVisionApplication::class.java.getResource(fxmlFile))
            val root: Parent = fxmlLoader.load()

            // Scene loading and title setup
            stage.scene = Scene(root, 500.0, 300.0)
            stage.title = title
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error changing scene: $fxmlFile")
        }
    }

    private fun getCurrentStage(): Stage {
        return Stage.getWindows().find { it.isFocused } as Stage
    }
}