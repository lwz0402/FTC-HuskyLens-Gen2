package org.firstinspires.ftc.teamcode.huskylens;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.*;
import java.util.List;

/**
 * HuskyLensMasterSample is a comprehensive OpMode demonstrating EVERY public method 
 * available in the HuskyLens Gen2 library.
 */
@TeleOp(name = "HuskyLens Gen2: Master Sample", group = "Concept")
public class HuskyLensMasterSample extends LinearOpMode {

    @Override
    public void runOpMode() {
        // 1. Initialization & Handshake
        HuskyLens2 huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");
        
        telemetry.addData("Status", "Checking connection...");
        telemetry.update();

        // Method: knock() - Handshake with the sensor
        if (!huskyLens.knock()) {
            telemetry.addData("Error", "HuskyLens not connected!");
            telemetry.update();
        }

        // 2. Algorithm Management
        // Method: selectAlgorithm(Algorithm)
        huskyLens.selectAlgorithm(Algorithm.ALGORITHM_FACE_RECOGNITION);
        
        // Method: setMultiAlgorithm(Algorithm...) - Run up to 3 algorithms at once
        huskyLens.setMultiAlgorithm(Algorithm.ALGORITHM_FACE_RECOGNITION, Algorithm.ALGORITHM_OBJECT_RECOGNITION);
        
        // Method: setMultiAlgorithmRatio(int...) - Set priority ratios for multi-algorithms
        huskyLens.setMultiAlgorithmRatio(50, 50);

        // 3. UI & Drawing (Shapes and Text)
        // Method: clearRect() - Clear protocol-drawn rectangles
        huskyLens.clearRect();
        // Method: drawRect(color, lineWidth, x, y, w, h)
        huskyLens.drawRect(HuskyLens2.COLOR_GREEN, 2, 20, 20, 100, 100);
        // Method: drawUniqueRect(...) - Draws one rect and clears previous protocol rects
        huskyLens.drawUniqueRect(HuskyLens2.COLOR_RED, 3, 150, 150, 60, 60);
        
        // Method: clearText() - Clear protocol-drawn text
        huskyLens.clearText();
        // Method: drawText(color, size, x, y, text)
        huskyLens.drawText(HuskyLens2.COLOR_CYAN, 2, 20, 130, "Master Sample Active");

        // 4. Algorithm Parameters
        // Method: setAlgorithmParam(Algorithm, key, value) - Support Boolean, Float, String
        huskyLens.setAlgorithmParam(Algorithm.ALGORITHM_FACE_RECOGNITION, "show_name", true);
        // Method: getAlgorithmParam(Algorithm, key)
        Object paramValue = huskyLens.getAlgorithmParam(Algorithm.ALGORITHM_FACE_RECOGNITION, "show_name");
        
        // Method: updateAlgorithmParams(Algorithm) - Refresh/Commit parameter changes
        huskyLens.updateAlgorithmParams(Algorithm.ALGORITHM_FACE_RECOGNITION);

        // 5. Data Management (IDs and Names)
        // Method: setNameByID(Algorithm, id, name) - Label a learned ID
        huskyLens.setNameByID(Algorithm.ALGORITHM_FACE_RECOGNITION, 1, "Player One");

        waitForStart();

        while (opModeIsActive()) {
            // 6. Result Retrieval
            // Method: requestBlocks(Algorithm)
            List<Block> blocks = huskyLens.requestBlocks(Algorithm.ALGORITHM_FACE_RECOGNITION);
            telemetry.addData("Blocks", blocks.size());
            
            for (Block block : blocks) {
                // Accessing Block properties: id, xCenter, yCenter, width, height, name, content, privateData
                telemetry.addData("ID " + block.id, "Pos: %d,%d Name: %s", block.xCenter, block.yCenter, block.name);
                
                // Example of using privateData (e.g., face landmarks)
                if (block.privateData != null && block.privateData.length > 0) {
                   telemetry.addData("Extra Data", "Bytes: " + block.privateData.length);
                }
            }

            // Method: requestArrows(Algorithm) - Used for Line Tracking
            List<Arrow> arrows = huskyLens.requestArrows(Algorithm.ALGORITHM_LINE_TRACKING);
            if (!arrows.isEmpty()) {
                Arrow arrow = arrows.get(0);
                // Accessing Arrow properties: id, xTarget, yTarget, angle, length
                telemetry.addData("Line Track", "Angle: %d", arrow.angle);
            }

            // 7. Multimedia & Capture
            if (gamepad1.x) {
                // Method: takePhoto(resolution)
                huskyLens.takePhoto(HuskyLens2.RESOLUTION_1280x720);
                // Method: takeScreenshot()
                huskyLens.takeScreenshot();
            }

            if (gamepad1.y) {
                // Method: playMusic(name, volume)
                huskyLens.playMusic("alert.mp3", 80);
            }

            // 8. Interactive Learning & Knowledge Base
            if (gamepad1.a) {
                // Method: learn(Algorithm) - Learns what's currently in center
                int newId = huskyLens.learn(Algorithm.ALGORITHM_FACE_RECOGNITION);
                // Method: saveKnowledge(Algorithm, slot) - Save to one of 5 slots (0-4)
                huskyLens.saveKnowledge(Algorithm.ALGORITHM_FACE_RECOGNITION, 0);
            }

            if (gamepad1.b) {
                // Method: learnBlock(Algorithm, x, y, w, h) - Learn a specific region
                huskyLens.learnBlock(Algorithm.ALGORITHM_OBJECT_TRACKING, 320, 240, 100, 100);
                // Method: loadKnowledge(Algorithm, slot)
                huskyLens.loadKnowledge(Algorithm.ALGORITHM_FACE_RECOGNITION, 0);
            }

            if (gamepad1.back) {
                // Method: forget() - Reset learned data for current algorithm
                huskyLens.forget();
            }

            // 9. Recording
            if (gamepad1.left_bumper) {
                // Method: startRecording(mediaType, duration, filename, resolution)
                huskyLens.startRecording(HuskyLens2.MEDIA_TYPE_VIDEO, 0, "test.mp4", HuskyLens2.RESOLUTION_1280x720);
            }
            if (gamepad1.right_bumper) {
                // Method: stopRecording(mediaType)
                huskyLens.stopRecording(HuskyLens2.MEDIA_TYPE_VIDEO);
            }

            telemetry.update();
            sleep(50);
        }

        // 10. Termination
        // Method: exit() - Exit current app to main menu
        huskyLens.exit();
    }
}
