/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Multiple objects controller class
 */

package dev.namexdd.machinevision_hw.multiple_objects

import dev.namexdd.machinevision_hw.MachineVisionApplication
import dev.namexdd.machinevision_hw.multiple_objects.ImageProcessor
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
            startButton.isDisable = false // Активировать кнопку "Start Image Processing"
        } else {
            fileNameLabel.text = "No valid file selected."
            startButton.isDisable = true // Деактивировать кнопку "Start Image Processing"
        }
    }

    fun startImageProcessing() {
        if (selectedFile != null) {
            try {
                // Path to the template image in resources
                val templateURL = MachineVisionApplication::class.java.getResource("/dev/namexdd/machinevision_hw/images/patterns/template.png")
                    ?: throw IllegalArgumentException("Template image not found.")
                val templatePath = File(templateURL.toURI()).absolutePath

                // Analyze the image with the template
                val result = ImageProcessor.analyzeImage(
                    imagePath = selectedFile!!.absolutePath,
                    templatePath = templatePath,
                )

                // Display the results
                ResultViewer.show(result, selectedFile!!.name)
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
        return MachineVisionApplication.primaryStage
            ?: throw IllegalStateException("Primary stage is not initialized")
    }
}
