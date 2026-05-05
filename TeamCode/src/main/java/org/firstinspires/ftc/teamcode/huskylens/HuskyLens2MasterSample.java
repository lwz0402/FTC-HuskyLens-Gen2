package org.firstinspires.ftc.teamcode.huskylens;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Algorithm;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Arrow;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Block;

import java.util.List;

/**
 * Comprehensive FTC sample for the HuskyLens Gen2 Java driver.
 *
 * Controls:
 * - D-pad left: Tag Recognition
 * - D-pad right: Face Recognition
 * - D-pad up: Line Tracking
 * - D-pad down: Object Tracking
 * - X: Take photo + screenshot
 * - Y: Play music
 * - A: Learn current target and save slot 0
 * - B: Load slot 0, or learn a centered tracking box in Object Tracking
 * - Back: Forget current learned data
 * - Left bumper: Start video recording
 * - Right bumper: Stop video recording
 */
@TeleOp(name = "HuskyLens Gen2: Master Sample", group = "Concept")
public class HuskyLens2MasterSample extends LinearOpMode {

    private static final int TRACK_BOX_WIDTH = 100;
    private static final int TRACK_BOX_HEIGHT = 100;
    private static final int TRACK_BOX_X = (HuskyLens2.FRAME_WIDTH - TRACK_BOX_WIDTH) / 2;
    private static final int TRACK_BOX_Y = (HuskyLens2.FRAME_HEIGHT - TRACK_BOX_HEIGHT) / 2;

    @Override
    public void runOpMode() {
        HuskyLens2 huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");
        Algorithm activeAlgorithm = Algorithm.ALGORITHM_TAG_RECOGNITION;
        String lastAction = "Idle";
        Boolean faceShowName = null;

        telemetry.addData("Status", "Connecting to HuskyLens Gen2...");
        telemetry.update();

        if (!huskyLens.knock()) {
            telemetry.addData("Error", "HuskyLens Gen2 is not responding on I2C.");
            telemetry.update();
            return;
        }

        if (!huskyLens.selectAlgorithm(activeAlgorithm)) {
            telemetry.addData("Error", "Failed to switch the startup algorithm.");
            telemetry.update();
            return;
        }

        huskyLens.clearRect();
        huskyLens.clearText();
        huskyLens.drawUniqueRect(HuskyLens2.COLOR_GREEN, 2, 12, 12, 180, 60);
        huskyLens.drawText(HuskyLens2.COLOR_CYAN, 2, 24, 30, "FTC HuskyLens2 Ready");

        telemetry.addData("Status", "Ready");
        telemetry.addData("Startup Algorithm", activeAlgorithm);
        telemetry.update();

        waitForStart();

        boolean prevDpadLeft = false;
        boolean prevDpadRight = false;
        boolean prevDpadUp = false;
        boolean prevDpadDown = false;
        boolean prevX = false;
        boolean prevY = false;
        boolean prevA = false;
        boolean prevB = false;
        boolean prevBack = false;
        boolean prevLeftBumper = false;
        boolean prevRightBumper = false;

        while (opModeIsActive()) {
            if (gamepad1.dpad_left && !prevDpadLeft) {
                if (huskyLens.selectAlgorithm(Algorithm.ALGORITHM_TAG_RECOGNITION)) {
                    activeAlgorithm = Algorithm.ALGORITHM_TAG_RECOGNITION;
                    lastAction = "Switched to Tag Recognition";
                } else {
                    lastAction = "Failed to switch to Tag Recognition";
                }
            }

            if (gamepad1.dpad_right && !prevDpadRight) {
                if (huskyLens.selectAlgorithm(Algorithm.ALGORITHM_FACE_RECOGNITION)) {
                    activeAlgorithm = Algorithm.ALGORITHM_FACE_RECOGNITION;
                    huskyLens.setAlgorithmParam(activeAlgorithm, "show_name", true);
                    huskyLens.updateAlgorithmParams(activeAlgorithm);
                    faceShowName = huskyLens.getAlgorithmParamBoolean(activeAlgorithm, "show_name");
                    lastAction = "Switched to Face Recognition";
                } else {
                    lastAction = "Failed to switch to Face Recognition";
                }
            }

            if (gamepad1.dpad_up && !prevDpadUp) {
                if (huskyLens.selectAlgorithm(Algorithm.ALGORITHM_LINE_TRACKING)) {
                    activeAlgorithm = Algorithm.ALGORITHM_LINE_TRACKING;
                    lastAction = "Switched to Line Tracking";
                } else {
                    lastAction = "Failed to switch to Line Tracking";
                }
            }

            if (gamepad1.dpad_down && !prevDpadDown) {
                if (huskyLens.selectAlgorithm(Algorithm.ALGORITHM_OBJECT_TRACKING)) {
                    activeAlgorithm = Algorithm.ALGORITHM_OBJECT_TRACKING;
                    huskyLens.drawUniqueRect(
                            HuskyLens2.COLOR_YELLOW,
                            2,
                            TRACK_BOX_X,
                            TRACK_BOX_Y,
                            TRACK_BOX_WIDTH,
                            TRACK_BOX_HEIGHT
                    );
                    lastAction = "Switched to Object Tracking";
                } else {
                    lastAction = "Failed to switch to Object Tracking";
                }
            }

            if (gamepad1.x && !prevX) {
                String photo = huskyLens.takePhoto();
                String screenshot = huskyLens.takeScreenshot();
                lastAction = "Photo=" + showValue(photo) + ", Screenshot=" + showValue(screenshot);
            }

            if (gamepad1.y && !prevY) {
                boolean ok = huskyLens.playMusic("alert.mp3", 80);
                lastAction = ok ? "playMusic(alert.mp3) succeeded" : "playMusic(alert.mp3) failed";
            }

            if (gamepad1.a && !prevA) {
                int learnedId = huskyLens.learn(activeAlgorithm);
                boolean saved = learnedId > 0 && huskyLens.saveKnowledge(activeAlgorithm, 0);
                lastAction = "learn=" + learnedId + ", saveSlot0=" + saved;
            }

            if (gamepad1.b && !prevB) {
                if (activeAlgorithm == Algorithm.ALGORITHM_OBJECT_TRACKING) {
                    int learnedId = huskyLens.learnBlock(
                            activeAlgorithm,
                            TRACK_BOX_X,
                            TRACK_BOX_Y,
                            TRACK_BOX_WIDTH,
                            TRACK_BOX_HEIGHT
                    );
                    lastAction = "learnBlock=" + learnedId;
                } else {
                    boolean loaded = huskyLens.loadKnowledge(activeAlgorithm, 0);
                    lastAction = "loadSlot0=" + loaded;
                }
            }

            if (gamepad1.back && !prevBack) {
                boolean forgot = huskyLens.forget(activeAlgorithm);
                lastAction = "forget=" + forgot;
            }

            if (gamepad1.left_bumper && !prevLeftBumper) {
                boolean started = huskyLens.startRecording(
                        HuskyLens2.MEDIA_TYPE_VIDEO,
                        0,
                        "ftc_huskylens.mp4",
                        HuskyLens2.RESOLUTION_1280x720
                );
                lastAction = "startRecording=" + started;
            }

            if (gamepad1.right_bumper && !prevRightBumper) {
                boolean stopped = huskyLens.stopRecording(HuskyLens2.MEDIA_TYPE_VIDEO);
                lastAction = "stopRecording=" + stopped;
            }

            telemetry.addData("Active Algorithm", activeAlgorithm);
            telemetry.addData("Last Action", lastAction);
            telemetry.addData("Controls", "D-pad switch algorithms, X/Y/A/B/Back/LB/RB run commands");

            if (activeAlgorithm == Algorithm.ALGORITHM_LINE_TRACKING) {
                List<Arrow> arrows = huskyLens.requestArrows(activeAlgorithm);
                telemetry.addData("Arrows", arrows.size());
                for (int i = 0; i < Math.min(arrows.size(), 3); i++) {
                    Arrow arrow = arrows.get(i);
                    telemetry.addData(
                            "Arrow " + i,
                            "level=%d target=(%d,%d) angle=%d len=%d",
                            arrow.level,
                            arrow.xTarget,
                            arrow.yTarget,
                            arrow.angle,
                            arrow.length
                    );
                }
            } else {
                List<Block> blocks = huskyLens.requestBlocks(activeAlgorithm);
                telemetry.addData("Blocks", blocks.size());
                for (int i = 0; i < Math.min(blocks.size(), 4); i++) {
                    Block block = blocks.get(i);
                    telemetry.addData(
                            "Block " + i,
                            "id=%d center=(%d,%d) size=%dx%d name=%s content=%s",
                            block.id,
                            block.xCenter,
                            block.yCenter,
                            block.width,
                            block.height,
                            showValue(block.name),
                            showValue(block.content)
                    );
                    if (block.privateData.length > 0) {
                        telemetry.addData("Block " + i + " Extra", "%d bytes", block.privateData.length);
                    }
                }
            }

            if (faceShowName != null) {
                telemetry.addData("Face Param show_name", faceShowName);
            }

            telemetry.update();

            prevDpadLeft = gamepad1.dpad_left;
            prevDpadRight = gamepad1.dpad_right;
            prevDpadUp = gamepad1.dpad_up;
            prevDpadDown = gamepad1.dpad_down;
            prevX = gamepad1.x;
            prevY = gamepad1.y;
            prevA = gamepad1.a;
            prevB = gamepad1.b;
            prevBack = gamepad1.back;
            prevLeftBumper = gamepad1.left_bumper;
            prevRightBumper = gamepad1.right_bumper;

            sleep(50);
        }

        huskyLens.exit();
    }

    private String showValue(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}
