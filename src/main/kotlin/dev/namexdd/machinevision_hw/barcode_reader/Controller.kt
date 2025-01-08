/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Barcode controller class
 */

package dev.namexdd.machinevision_hw.barcode_reader

import dev.namexdd.machinevision_hw.utils.FileSelector
import dev.namexdd.machinevision_hw.utils.Navigator
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.stage.Stage
import java.io.File

class Controller {

    @FXML
    private lateinit var descriptionLabel: Label

    @FXML
    private lateinit var fileNameLabel: Label

    @FXML
    private lateinit var startButton: Button

    private var selectedFile: File? = null

    fun chooseFile() {
        selectedFile = FileSelector.selectAndValidateFile(
            getCurrentStage(),
            "Select Image",
            listOf("png", "jpg", "jpeg")
        )

        if (selectedFile != null) {
            fileNameLabel.text = "Selected file: ${selectedFile!!.name}"
            startButton.isDisable = false // Activate "Start Image Processing" button
        } else {
            fileNameLabel.text = "No valid file selected."
            startButton.isDisable = true // Deactivate "Start Image Processing" button
        }
    }

    fun startImageProcessing() {
        if (selectedFile != null) {
            try {
                // Process the image
                val result = ImageProcessor.analyzeImage(selectedFile!!.absolutePath, selectedFile!!.name)

                // Display the results
                ResultViewer.show(result)
            } catch (e: Exception) {
                println("Error processing image: ${e.message}")
                fileNameLabel.text = "Error processing image. Please try again."
            }
        } else {
            fileNameLabel.text = "No file selected. Please select a file."
        }
    }

    fun goBack() {
        Navigator.goBack()
    }

    private fun getCurrentStage(): Stage {
        return dev.namexdd.machinevision_hw.MachineVisionApplication.primaryStage
            ?: throw IllegalStateException("Primary stage is not initialized")
    }
}