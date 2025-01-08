/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: FileSelector is a utility class that provides a method for selecting and validating files
 */

package dev.namexdd.machinevision_hw.utils

import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

object FileSelector {

    /**
     * Opens a file chooser dialog, validates the selected file, and returns the file if valid.
     *
     * @param stage The current Stage where the dialog will be displayed.
     * @param title The title of the file chooser dialog.
     * @param allowedExtensions List of allowed file extensions for validation.
     * @return The selected valid File or null if no file was selected or validation failed.
     */

    fun selectAndValidateFile(stage: Stage, title: String, allowedExtensions: List<String>): File? {
        val fileChooser = FileChooser()
        fileChooser.title = title
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter("Allowed Files", allowedExtensions.map { "*.$it" })
        )

        val file = fileChooser.showOpenDialog(stage)
        if (file != null && validateFile(file, allowedExtensions)) {
            println("File selected: ${file.absolutePath}")
            return file
        } else {
            if (file != null) {
                println("Invalid file type: ${file.absolutePath}")
            } else {
                println("No file selected.")
            }
            return null
        }
    }

    /**
     * Validates if the selected file matches one of the allowed extensions.
     *
     * @param file The file to validate.
     * @param allowedExtensions List of allowed file extensions.
     * @return True if the file is valid, otherwise false.
     */

    private fun validateFile(file: File, allowedExtensions: List<String>): Boolean {
        val extension = file.extension.lowercase()
        return allowedExtensions.contains(extension)
    }
}
