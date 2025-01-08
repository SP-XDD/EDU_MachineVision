/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Barcode reader result viewer
 */

package dev.namexdd.machinevision_hw.barcode_reader

import dev.namexdd.machinevision_hw.MachineVisionApplication
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.opencv.imgcodecs.Imgcodecs
import java.io.File
import java.nio.file.Files

object ResultViewer {

    fun show(result: AnalysisResult) {
        val stage = Stage()
        stage.title = "Analysis Results"

        // Save annotated image temporarily
        val tempFile = File.createTempFile("processed", ".png")
        Imgcodecs.imwrite(tempFile.absolutePath, result.annotatedImage)

        // Display annotated image
        val imageView = ImageView(Image(tempFile.toURI().toString()))
        imageView.fitWidth = 400.0
        imageView.isPreserveRatio = true

        // Display stats
        val stats = buildString {
            appendLine("File Name: ${result.barcodeData ?: "No Barcode Found"}")
            appendLine("Processing Time: ${result.processingTimeMs} ms")
            appendLine("Image Size: ${result.imageSize.width.toInt()}x${result.imageSize.height.toInt()}")
            appendLine("Category: ${result.category}")
        }
        val statsLabel = Label(stats)

        // Highlight "Defective Parts" category
        if (result.category == "Defective Parts") {
            statsLabel.textFill = Color.RED
            statsLabel.text += "\nDEFECTIVE"
        }

        // Save results button
        val saveButton = Button("Save Results")
        saveButton.setOnAction {
            val fileChooser = FileChooser()
            fileChooser.title = "Save Results"
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Text Files", "*.txt"))
            val selectedFile = fileChooser.showSaveDialog(stage)
            if (selectedFile != null) {
                saveResultsToFile(selectedFile, stats, tempFile)
            }
        }

        // Layout setup
        val layout = VBox(10.0, imageView, statsLabel, saveButton)
        layout.spacing = 10.0
        layout.prefWidth = 600.0
        stage.icons.add(Image(MachineVisionApplication::class.java.getResourceAsStream("/dev/namexdd/machinevision_hw/images/appicon.png")))
        stage.scene = Scene(layout, 600.0, 800.0)
        stage.setOnCloseRequest { Files.deleteIfExists(tempFile.toPath()) }
        stage.show()
    }

    private fun saveResultsToFile(file: File, stats: String, imageFile: File) {
        try {
            // Save text information
            file.writeText(stats)

            // Save processed image in the same directory
            val imageCopy = File(file.parentFile, "${file.nameWithoutExtension}_image.png")
            imageFile.copyTo(imageCopy, overwrite = true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}