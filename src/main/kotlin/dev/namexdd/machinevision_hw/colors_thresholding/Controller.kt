/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Colors thresholding controller class
 */

package dev.namexdd.machinevision_hw.colors_thresholding

import dev.namexdd.machinevision_hw.MachineVisionApplication
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
    private val minPercent = 10.0 // Укажите минимальный процент для анализа

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
        selectedFile?.let { file ->
            try {
                // Process the image
                val result = ImageProcessor.analyzeImage(
                    imagePath = file.absolutePath,
                    fileName = file.name,
                    minPercent = minPercent
                )

                // Display the results
                ResultViewer.show(result, file.name, minPercent)
            } catch (e: Exception) {
                println("Error processing image: ${e.message}")
                fileNameLabel.text = "Error processing image. Please try again."
            }
        }
    }

    fun goBack() {
        Navigator.goBack()
    }

    private fun getCurrentStage(): Stage {
        return MachineVisionApplication.primaryStage
            ?: throw IllegalStateException("Primary stage is not initialized")
    }
}