/*
    Created by Sander Paju for TalTech
    Machine Vision. Lecturer: Daniil Valme

    About: Multiple objects image processor class

    This class processes an image to detect multiple objects that match a template (which can be scaled and rotated).
    The process works in the following steps:

    1. Load the input image and template image.
    2. Define the scales, angles, and a matching threshold for the template matching.
    3. Process the image using multi-threading to try various combinations of template scales and rotations.
    4. Perform template matching for each combination and store matching regions (bounding boxes).
    5. Merge overlapping bounding boxes to group detected objects.
    6. Annotate the input image with bounding boxes around detected objects.
    7. Return the annotated image, processing time, image size, and the number of detected objects.
 */

package dev.namexdd.machinevision_hw.multiple_objects

import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors

// Data class to hold the results of the image analysis
data class AnalysisResult(
    val annotatedImage: Mat, // Image with bounding boxes annotated
    val processingTimeMs: Long, // Time taken for processing in milliseconds
    val imageSize: Size, // Size of the image (width and height)
    val detectedObjectsCount: Int // Number of objects detected in the image
)

object ImageProcessor {

    // Main method to analyze the image and match objects with the template
    fun analyzeImage(imagePath: String, templatePath: String): AnalysisResult {
        val startTime = System.currentTimeMillis() // Start time for processing

        // Step 1: Load the input image and template image
        val inputImage = Imgcodecs.imread(imagePath)
        val templateImage = Imgcodecs.imread(templatePath)
        if (inputImage.empty() || templateImage.empty()) {
            throw IllegalArgumentException("One or both images could not be loaded.") // Throw an error if images cannot be loaded
        }

        val imageSize = inputImage.size() // Get the size of the input image
        val annotatedImage = inputImage.clone() // Clone the input image to annotate it later

        // Step 2: Define the scales, angles, and matching threshold
        val scales = listOf(1.0, 0.8, 0.6) // List of scales to apply to the template
        val angles = (0 until 360 step 10).map { it.toDouble() } // List of angles for rotation (from 0 to 350 degrees)
        val threshold = 0.8 // Matching threshold for template matching (higher value means stricter match)

        val detectedRects = mutableListOf<Rect>() // List to store bounding rectangles of detected objects
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) // Create a thread pool to process the image concurrently

        // Step 3: Process the image using multi-threading (for each scale and angle)
        for (scale in scales) {
            executor.submit {
                for (angle in angles) {
                    val transformedTemplate = scaleAndRotateTemplate(templateImage, scale, angle) // Scale and rotate the template

                    if (transformedTemplate.cols() < 10 || transformedTemplate.rows() < 10) continue // Skip small templates

                    val result = Mat() // Mat to store the result of the template matching
                    Imgproc.matchTemplate(inputImage, transformedTemplate, result, Imgproc.TM_CCOEFF_NORMED) // Perform template matching

                    // Step 4: Search for matches (detect regions with a high correlation score)
                    for (y in 0 until result.rows()) {
                        for (x in 0 until result.cols()) {
                            if (result.get(y, x)[0] >= threshold) { // If match score is above the threshold
                                synchronized(detectedRects) { // Synchronize to avoid concurrent modification of the list
                                    detectedRects.add(Rect(x, y, transformedTemplate.cols(), transformedTemplate.rows())) // Add detected region
                                }
                            }
                        }
                    }
                }
            }
        }

        executor.shutdown() // Shutdown the executor to stop further tasks
        while (!executor.isTerminated) {
            Thread.sleep(10) // Wait for all tasks to complete
        }

        // Step 5: Merge overlapping bounding boxes to group detected objects
        val mergedRects = mergeBoundingBoxes(detectedRects)

        // Step 6: Annotate the input image with bounding boxes around detected objects
        mergedRects.forEach { rect ->
            Imgproc.rectangle(
                annotatedImage,
                Point(rect.x.toDouble(), rect.y.toDouble()),
                Point((rect.x + rect.width).toDouble(), (rect.y + rect.height).toDouble()),
                Scalar(0.0, 255.0, 0.0), // Draw rectangle in green color
                2
            )
        }

        val endTime = System.currentTimeMillis() // End time for processing
        return AnalysisResult(
            annotatedImage = annotatedImage, // Annotated image with bounding boxes
            processingTimeMs = endTime - startTime, // Processing time in milliseconds
            imageSize = imageSize, // Size of the input image
            detectedObjectsCount = mergedRects.size // Number of detected objects (bounding boxes)
        )
    }

    // Step 7: Function to scale and rotate the template image
    private fun scaleAndRotateTemplate(template: Mat, scale: Double, angle: Double): Mat {
        val resizedTemplate = Mat() // Mat to store the resized template
        Imgproc.resize(
            template,
            resizedTemplate,
            Size(template.cols() * scale, template.rows() * scale) // Resize the template based on the scale
        )

        val rotatedTemplate = Mat() // Mat to store the rotated template
        val center = Point(resizedTemplate.cols() / 2.0, resizedTemplate.rows() / 2.0) // Calculate the center of the resized template
        val rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0) // Get the rotation matrix for the specified angle
        Imgproc.warpAffine(resizedTemplate, rotatedTemplate, rotationMatrix, resizedTemplate.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, Scalar(0.0, 0.0, 0.0)) // Rotate the template

        return rotatedTemplate // Return the rotated template
    }

    // Step 8: Function to merge overlapping bounding boxes
    private fun mergeBoundingBoxes(rects: List<Rect>): List<Rect> {
        if (rects.isEmpty()) return emptyList() // Return an empty list if no rectangles were detected
        val merged = mutableListOf<Rect>() // List to store merged rectangles
        val sortedRects = rects.sortedBy { it.x } // Sort rectangles by their x-coordinate
        var currentRect = sortedRects[0] // Start with the first rectangle

        for (i in 1 until sortedRects.size) {
            val nextRect = sortedRects[i]
            if (areOverlapping(currentRect, nextRect)) { // Check if the current rectangle overlaps with the next one
                currentRect = currentRect.union(nextRect) // Merge the two rectangles
            } else {
                merged.add(currentRect) // Add the non-overlapping rectangle to the merged list
                currentRect = nextRect // Move to the next rectangle
            }
        }
        merged.add(currentRect) // Add the last rectangle to the list
        return merged // Return the list of merged rectangles
    }

    // Step 9: Helper function to check if two rectangles overlap
    private fun areOverlapping(rect1: Rect, rect2: Rect): Boolean {
        val xOverlap = rect1.x < rect2.x + rect2.width && rect1.x + rect1.width > rect2.x
        val yOverlap = rect1.y < rect2.y + rect2.height && rect1.y + rect1.height > rect2.y
        return xOverlap && yOverlap // Return true if there is overlap in both x and y directions
    }

    // Step 10: Helper function to combine two overlapping rectangles into one
    private fun Rect.union(other: Rect): Rect {
        val x1 = minOf(this.x, other.x)
        val y1 = minOf(this.y, other.y)
        val x2 = maxOf(this.x + this.width, other.x + other.width)
        val y2 = maxOf(this.y + this.height, other.y + other.height)
        return Rect(x1, y1, x2 - x1, y2 - y1) // Return a rectangle that bounds both rectangles
    }
}