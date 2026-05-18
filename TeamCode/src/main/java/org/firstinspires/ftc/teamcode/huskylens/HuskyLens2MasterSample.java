package org.firstinspires.ftc.teamcode.huskylens;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.I2cAddr;

import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Algorithm;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Arrow;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Block;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Info;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.ReadStatus;

import java.util.List;

/**
 * Teaching sample for the HuskyLens2 FTC Gen2 driver.
 *
 * Algorithm usage:
 * - Call selectAlgorithm(algorithm) whenever you want the camera to run a mode.
 * - Most algorithms return Block results through getCachedBlocks() or requestBlocks().
 * - LINE_TRACKING returns Arrow results through getCachedArrows() or requestArrows().
 * - ALGORITHM_ANY is kept for general commands; it is not used as a normal vision mode here.
 *
 * Recommended result loop:
 * - beginResultRequest(activeAlgorithm) once.
 * - pollResultRequest(maxPackets, maxMillis) every OpMode loop with a small budget.
 * - Read getCachedInfo(), getCachedBlocks(), getCachedArrows(), and getLastReadStatus().
 *
 * Compatibility result calls:
 * - requestInfo(), requestBlocks(), and requestArrows() still work, but they use a short bounded wait.
 */
@TeleOp(name = "HuskyLens2: Master Sample", group = "Concept")
public class HuskyLens2MasterSample extends LinearOpMode {

    private static final Algorithm[] VISION_ALGORITHMS = {
            Algorithm.ALGORITHM_FACE_RECOGNITION,
            Algorithm.ALGORITHM_OBJECT_RECOGNITION,
            Algorithm.ALGORITHM_OBJECT_TRACKING,
            Algorithm.ALGORITHM_COLOR_RECOGNITION,
            Algorithm.ALGORITHM_OBJECT_CLASSIFICATION,
            Algorithm.ALGORITHM_SELF_LEARNING_CLASSIFICATION,
            Algorithm.ALGORITHM_SEGMENT,
            Algorithm.ALGORITHM_HAND_RECOGNITION,
            Algorithm.ALGORITHM_POSE_RECOGNITION,
            Algorithm.ALGORITHM_LICENSE_RECOGNITION,
            Algorithm.ALGORITHM_OCR_RECOGNITION,
            Algorithm.ALGORITHM_LINE_TRACKING,
            Algorithm.ALGORITHM_EMOTION_RECOGNITION,
            Algorithm.ALGORITHM_GAZE_RECOGNITION,
            Algorithm.ALGORITHM_FACE_ORIENTATION,
            Algorithm.ALGORITHM_TAG_RECOGNITION,
            Algorithm.ALGORITHM_BARCODE_RECOGNITION,
            Algorithm.ALGORITHM_QRCODE_RECOGNITION,
            Algorithm.ALGORITHM_FALLDOWN_RECOGNITION,
            Algorithm.ALGORITHM_DEPTH_CAMERA,
            Algorithm.ALGORITHM_DONKEYCAR,
            Algorithm.ALGORITHM_CAMERA,
            Algorithm.ALGORITHM_RFU3,
            Algorithm.ALGORITHM_RFU4,
            Algorithm.ALGORITHM_CUSTOM0,
            Algorithm.ALGORITHM_CUSTOM1,
            Algorithm.ALGORITHM_CUSTOM2
    };

    private static final int TRACK_BOX_WIDTH = 100;
    private static final int TRACK_BOX_HEIGHT = 100;
    private static final int TRACK_BOX_X = (HuskyLens2.FRAME_WIDTH - TRACK_BOX_WIDTH) / 2;
    private static final int TRACK_BOX_Y = (HuskyLens2.FRAME_HEIGHT - TRACK_BOX_HEIGHT) / 2;
    private static final int KNOWLEDGE_SLOT = 0;
    private static final int POLL_MAX_PACKETS = 2;
    private static final long POLL_TIME_BUDGET_MS = 4;

    private HuskyLens2 huskyLens;
    private int algorithmIndex = 15;
    private Algorithm activeAlgorithm = VISION_ALGORITHMS[algorithmIndex];
    private boolean resultRequestStarted = false;
    private String lastAction = "Idle";

    @Override
    public void runOpMode() {
        huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");

        HuskyLens2.Parameters parameters = huskyLens.getParameters();
        parameters.defaultPollTimeBudgetMs = POLL_TIME_BUDGET_MS;
        parameters.maxCachedResults = 24;
        parameters.commandTimeoutMs = 120;
        huskyLens.initialize(parameters);

        telemetry.addData("Status", "Connecting to HuskyLens2");
        telemetry.addData("I2C Address", huskyLens.getI2cAddress());
        telemetry.update();

        if (!huskyLens.knock()) {
            telemetry.addData("Error", "HuskyLens2 did not answer knock()");
            telemetry.update();
            return;
        }

        if (!selectActiveAlgorithm(activeAlgorithm)) {
            telemetry.addData("Error", "Could not select startup algorithm");
            telemetry.update();
            return;
        }

        drawReadyOverlay();

        telemetry.addData("Status", "Ready");
        telemetry.addData("Startup Algorithm", activeAlgorithm);
        telemetry.update();

        waitForStart();

        ButtonEdges buttons = new ButtonEdges();
        resultRequestStarted = huskyLens.beginResultRequest(activeAlgorithm);

        while (opModeIsActive()) {
            buttons.update();
            handleAlgorithmButtons(buttons);
            handleFunctionButtons(buttons);
            pollActiveCamera();
            showTelemetry();
            telemetry.update();
            idle();
        }

        huskyLens.exit();
    }

    private void handleAlgorithmButtons(ButtonEdges buttons) {
        if (buttons.dpadRightPressed) {
            algorithmIndex = (algorithmIndex + 1) % VISION_ALGORITHMS.length;
            selectActiveAlgorithm(VISION_ALGORITHMS[algorithmIndex]);
        }

        if (buttons.dpadLeftPressed) {
            algorithmIndex = (algorithmIndex + VISION_ALGORITHMS.length - 1) % VISION_ALGORITHMS.length;
            selectActiveAlgorithm(VISION_ALGORITHMS[algorithmIndex]);
        }
    }

    private void handleFunctionButtons(ButtonEdges buttons) {
        if (buttons.aPressed) {
            lastAction = "knock=" + huskyLens.knock();
        }

        if (buttons.xPressed) {
            lastAction = runLearningAndKnowledgeSaveDemo();
        }

        if (buttons.bPressed) {
            lastAction = "loadKnowledge(slot0)=" + huskyLens.loadKnowledge(activeAlgorithm, KNOWLEDGE_SLOT);
        }

        if (buttons.yPressed) {
            lastAction = "forget(activeAlgorithm)=" + huskyLens.forget(activeAlgorithm);
        }

        if (buttons.leftBumperPressed) {
            lastAction = runOverlayDemo();
        }

        if (buttons.rightBumperPressed) {
            lastAction = runCaptureDemo();
        }

        if (buttons.leftTriggerPressed) {
            lastAction = "startRecording(video)=" + huskyLens.startRecording(
                    HuskyLens2.MEDIA_TYPE_VIDEO,
                    0,
                    "ftc_huskylens2.mp4",
                    HuskyLens2.RESOLUTION_1280x720
            );
        }

        if (buttons.rightTriggerPressed) {
            lastAction = "stopRecording(video)=" + huskyLens.stopRecording(HuskyLens2.MEDIA_TYPE_VIDEO);
        }

        if (buttons.startPressed) {
            lastAction = runAlgorithmParameterDemo();
        }

        if (buttons.backPressed) {
            lastAction = runMultiAlgorithmDemo();
        }

        if (buttons.leftStickPressed) {
            lastAction = "playMusic(alert.mp3)=" + huskyLens.playMusic("alert.mp3", 80);
        }

        if (buttons.rightStickPressed) {
            lastAction = "setNameByID(1)=" + huskyLens.setNameByID(activeAlgorithm, 1, "FTC Target");
        }
    }

    private boolean selectActiveAlgorithm(Algorithm algorithm) {
        boolean selected = huskyLens.selectAlgorithm(algorithm);
        if (selected) {
            activeAlgorithm = algorithm;
            resultRequestStarted = opModeIsActive() && huskyLens.beginResultRequest(activeAlgorithm);
            lastAction = "selectAlgorithm(" + algorithm + ")";
        } else {
            lastAction = "selectAlgorithm failed for " + algorithm;
        }
        return selected;
    }

    private void pollActiveCamera() {
        if (!resultRequestStarted) {
            resultRequestStarted = huskyLens.beginResultRequest(activeAlgorithm);
            return;
        }

        boolean complete = huskyLens.pollResultRequest(POLL_MAX_PACKETS, POLL_TIME_BUDGET_MS);
        ReadStatus status = huskyLens.getLastReadStatus();
        if (complete || isFinishedStatus(status)) {
            resultRequestStarted = huskyLens.beginResultRequest(activeAlgorithm);
        }
    }

    private void showTelemetry() {
        Info info = huskyLens.getCachedInfo();
        List<Block> blocks = huskyLens.getCachedBlocks();
        List<Arrow> arrows = huskyLens.getCachedArrows();

        telemetry.addData("Algorithm", "%d/%d %s", algorithmIndex + 1, VISION_ALGORITHMS.length, activeAlgorithm);
        telemetry.addData("Result Type", returnsArrows(activeAlgorithm) ? "Arrow" : "Block");
        telemetry.addData("Read Status", huskyLens.getLastReadStatus());
        telemetry.addData("Last Action", lastAction);

        if (info != null) {
            telemetry.addData(
                    "Info",
                    "total=%d blocks=%d learned=%d maxID=%d",
                    info.totalResults,
                    info.totalBlocks,
                    info.totalResultsLearned,
                    info.maxID
            );
        }

        if (returnsArrows(activeAlgorithm)) {
            telemetry.addData("Cached Arrows", arrows.size());
            for (int i = 0; i < Math.min(arrows.size(), 3); i++) {
                Arrow arrow = arrows.get(i);
                telemetry.addData(
                        "Arrow " + i,
                        "id=%d level=%d target=(%d,%d) angle=%d len=%d",
                        arrow.id,
                        arrow.level,
                        arrow.xTarget,
                        arrow.yTarget,
                        arrow.angle,
                        arrow.length
                );
            }
        } else {
            telemetry.addData("Cached Blocks", blocks.size());
            for (int i = 0; i < Math.min(blocks.size(), 4); i++) {
                Block block = blocks.get(i);
                telemetry.addData(
                        "Block " + i,
                        "id=%d algo=%s center=(%d,%d) size=%dx%d name=%s content=%s extra=%d",
                        block.id,
                        block.algorithm,
                        block.xCenter,
                        block.yCenter,
                        block.width,
                        block.height,
                        showValue(block.name),
                        showValue(block.content),
                        block.privateData.length
                );
            }
        }

        telemetry.addData("Algorithm Controls", "D-pad left/right cycle all Gen2 algorithms");
        telemetry.addData("Function Controls", "A knock, X learn/save, B load, Y forget, LB overlay, RB capture");
        telemetry.addData("More Controls", "LT start rec, RT stop rec, Start params, Back multi, sticks music/name");
    }

    private String runLearningAndKnowledgeSaveDemo() {
        int learnedId;
        if (activeAlgorithm == Algorithm.ALGORITHM_OBJECT_TRACKING) {
            learnedId = huskyLens.learnBlock(
                    activeAlgorithm,
                    TRACK_BOX_X,
                    TRACK_BOX_Y,
                    TRACK_BOX_WIDTH,
                    TRACK_BOX_HEIGHT
            );
        } else {
            learnedId = huskyLens.learn(activeAlgorithm);
        }

        boolean saved = learnedId > 0 && huskyLens.saveKnowledge(activeAlgorithm, KNOWLEDGE_SLOT);
        return "learn=" + learnedId + ", saveKnowledge(slot0)=" + saved;
    }

    private String runOverlayDemo() {
        boolean clearedRect = huskyLens.clearRect();
        boolean clearedText = huskyLens.clearText();
        boolean rect = huskyLens.drawRect(HuskyLens2.COLOR_GREEN, 2, 16, 16, 180, 70);
        boolean uniqueRect = huskyLens.drawUniqueRect(HuskyLens2.COLOR_YELLOW, 2, TRACK_BOX_X, TRACK_BOX_Y, TRACK_BOX_WIDTH, TRACK_BOX_HEIGHT);
        boolean text = huskyLens.drawText(HuskyLens2.COLOR_CYAN, 2, 24, 36, "FTC HuskyLens2");
        return "overlay clear=" + (clearedRect && clearedText) + ", draw=" + (rect && uniqueRect && text);
    }

    private String runCaptureDemo() {
        String photo = huskyLens.takePhoto(HuskyLens2.RESOLUTION_1280x720);
        String defaultPhoto = huskyLens.takePhoto();
        String screenshot = huskyLens.takeScreenshot();
        return "photo=" + showValue(photo) + ", defaultPhoto=" + showValue(defaultPhoto) + ", screenshot=" + showValue(screenshot);
    }

    private String runAlgorithmParameterDemo() {
        boolean setBool = huskyLens.setAlgorithmParam(activeAlgorithm, "show_name", true);
        boolean setFloat = huskyLens.setAlgorithmParam(activeAlgorithm, "confidence", 0.50f);
        boolean setString = huskyLens.setAlgorithmParam(activeAlgorithm, "label", "FTC");
        boolean setObject = huskyLens.setAlgorithmParam(activeAlgorithm, "enabled", Boolean.TRUE);
        boolean updated = huskyLens.updateAlgorithmParams(activeAlgorithm);
        Boolean boolValue = huskyLens.getAlgorithmParamBoolean(activeAlgorithm, "show_name");
        Float floatValue = huskyLens.getAlgorithmParamFloat(activeAlgorithm, "confidence");
        String stringValue = huskyLens.getAlgorithmParamString(activeAlgorithm, "label");
        Object genericValue = huskyLens.getAlgorithmParam(activeAlgorithm, "show_name");
        return "params set=" + (setBool && setFloat && setString && setObject)
                + ", update=" + updated
                + ", bool=" + boolValue
                + ", float=" + floatValue
                + ", string=" + showValue(stringValue)
                + ", generic=" + genericValue;
    }

    private String runMultiAlgorithmDemo() {
        boolean setAlgorithms = huskyLens.setMultiAlgorithm(
                Algorithm.ALGORITHM_TAG_RECOGNITION,
                Algorithm.ALGORITHM_FACE_RECOGNITION,
                Algorithm.ALGORITHM_LINE_TRACKING
        );
        boolean setRatios = huskyLens.setMultiAlgorithmRatio(1, 1, 1);
        return "setMultiAlgorithm=" + setAlgorithms + ", setMultiAlgorithmRatio=" + setRatios;
    }

    private void drawReadyOverlay() {
        huskyLens.clearRect();
        huskyLens.clearText();
        huskyLens.drawUniqueRect(HuskyLens2.COLOR_GREEN, 2, 12, 12, 210, 64);
        huskyLens.drawText(HuskyLens2.COLOR_CYAN, 2, 24, 32, "Ready");
    }

    private boolean returnsArrows(Algorithm algorithm) {
        return algorithm == Algorithm.ALGORITHM_LINE_TRACKING;
    }

    private boolean isFinishedStatus(ReadStatus status) {
        return status == ReadStatus.TIMEOUT
                || status == ReadStatus.TRUNCATED
                || status == ReadStatus.CHECKSUM_ERROR
                || status == ReadStatus.MALFORMED_PACKET
                || status == ReadStatus.PACKET_TOO_LARGE
                || status == ReadStatus.I2C_ERROR;
    }

    private String showValue(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    @SuppressWarnings("unused")
    private void customI2cAddressExample(HuskyLens2 lens) {
        HuskyLens2.Parameters parameters = lens.getParameters();
        parameters.i2cAddr = I2cAddr.create7bit(0x50);
        lens.initialize(parameters);
        lens.setI2cAddress(I2cAddr.create7bit(0x50));
    }

    @SuppressWarnings("unused")
    private void compatibilityReadExamples(HuskyLens2 lens) {
        boolean switched = lens.switchAlgorithm(Algorithm.ALGORITHM_TAG_RECOGNITION);
        Info info = lens.requestInfo(Algorithm.ALGORITHM_TAG_RECOGNITION);
        List<Block> blocks = lens.requestBlocks(Algorithm.ALGORITHM_TAG_RECOGNITION);
        List<Arrow> arrows = lens.requestArrows(Algorithm.ALGORITHM_LINE_TRACKING);
        boolean forgotAll = lens.forget();
        telemetry.addData("Compatibility switchAlgorithm", switched);
        telemetry.addData("Compatibility Info", info);
        telemetry.addData("Compatibility Blocks", blocks.size());
        telemetry.addData("Compatibility Arrows", arrows.size());
        telemetry.addData("Compatibility forget()", forgotAll);
    }

    @SuppressWarnings("unused")
    private void multipleHuskyLensOnOneHubExample() {
        HuskyLens2 front = hardwareMap.get(HuskyLens2.class, "frontHusky");
        HuskyLens2 rear = hardwareMap.get(HuskyLens2.class, "rearHusky");
        HuskyLens2 left = hardwareMap.get(HuskyLens2.class, "leftHusky");

        front.selectAlgorithm(Algorithm.ALGORITHM_TAG_RECOGNITION);
        rear.selectAlgorithm(Algorithm.ALGORITHM_OBJECT_RECOGNITION);
        left.selectAlgorithm(Algorithm.ALGORITHM_LINE_TRACKING);

        front.beginResultRequest(Algorithm.ALGORITHM_TAG_RECOGNITION);
        rear.beginResultRequest(Algorithm.ALGORITHM_OBJECT_RECOGNITION);
        left.beginResultRequest(Algorithm.ALGORITHM_LINE_TRACKING);

        while (opModeIsActive()) {
            front.pollResultRequest(1, 2);
            rear.pollResultRequest(1, 2);
            left.pollResultRequest(1, 2);

            List<Block> frontTags = front.getCachedBlocks();
            List<Block> rearObjects = rear.getCachedBlocks();
            List<Arrow> leftLine = left.getCachedArrows();

            telemetry.addData("frontHusky tags", frontTags.size());
            telemetry.addData("rearHusky objects", rearObjects.size());
            telemetry.addData("leftHusky arrows", leftLine.size());
            telemetry.update();
        }
    }

    private final class ButtonEdges {
        boolean dpadLeftPressed;
        boolean dpadRightPressed;
        boolean aPressed;
        boolean bPressed;
        boolean xPressed;
        boolean yPressed;
        boolean backPressed;
        boolean startPressed;
        boolean leftBumperPressed;
        boolean rightBumperPressed;
        boolean leftTriggerPressed;
        boolean rightTriggerPressed;
        boolean leftStickPressed;
        boolean rightStickPressed;

        private boolean previousDpadLeft;
        private boolean previousDpadRight;
        private boolean previousA;
        private boolean previousB;
        private boolean previousX;
        private boolean previousY;
        private boolean previousBack;
        private boolean previousStart;
        private boolean previousLeftBumper;
        private boolean previousRightBumper;
        private boolean previousLeftTrigger;
        private boolean previousRightTrigger;
        private boolean previousLeftStick;
        private boolean previousRightStick;

        void update() {
            boolean leftTrigger = gamepad1.left_trigger > 0.5;
            boolean rightTrigger = gamepad1.right_trigger > 0.5;

            dpadLeftPressed = gamepad1.dpad_left && !previousDpadLeft;
            dpadRightPressed = gamepad1.dpad_right && !previousDpadRight;
            aPressed = gamepad1.a && !previousA;
            bPressed = gamepad1.b && !previousB;
            xPressed = gamepad1.x && !previousX;
            yPressed = gamepad1.y && !previousY;
            backPressed = gamepad1.back && !previousBack;
            startPressed = gamepad1.start && !previousStart;
            leftBumperPressed = gamepad1.left_bumper && !previousLeftBumper;
            rightBumperPressed = gamepad1.right_bumper && !previousRightBumper;
            leftTriggerPressed = leftTrigger && !previousLeftTrigger;
            rightTriggerPressed = rightTrigger && !previousRightTrigger;
            leftStickPressed = gamepad1.left_stick_button && !previousLeftStick;
            rightStickPressed = gamepad1.right_stick_button && !previousRightStick;

            previousDpadLeft = gamepad1.dpad_left;
            previousDpadRight = gamepad1.dpad_right;
            previousA = gamepad1.a;
            previousB = gamepad1.b;
            previousX = gamepad1.x;
            previousY = gamepad1.y;
            previousBack = gamepad1.back;
            previousStart = gamepad1.start;
            previousLeftBumper = gamepad1.left_bumper;
            previousRightBumper = gamepad1.right_bumper;
            previousLeftTrigger = leftTrigger;
            previousRightTrigger = rightTrigger;
            previousLeftStick = gamepad1.left_stick_button;
            previousRightStick = gamepad1.right_stick_button;
        }
    }
}
