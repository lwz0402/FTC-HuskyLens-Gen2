package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Algorithm;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.Block;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.ReadStatus;

import java.util.List;

/**
 * Practical AprilTag example for HuskyLens Gen2:
 * 1. Switch HuskyLens to Tag Recognition.
 * 2. Pick the detected tag closest to the optical center.
 * 3. Pan a servo until the tag is centered.
 * 4. Only run the shooter after the target has stayed centered for a few frames.
 */
@TeleOp(name = "HuskyLens Gen2: AprilTag Tracker", group = "Practical")
public class HuskyLens2TagTracker extends LinearOpMode {

    private static final double SERVO_CENTER = 0.50;
    private static final double SERVO_STEP = 0.005;
    private static final double SERVO_MIN = 0.00;
    private static final double SERVO_MAX = 1.00;
    private static final double PAN_ERROR_TO_SERVO_SIGN = -1.0;

    private static final int CAMERA_CENTER_X = HuskyLens2.FRAME_WIDTH / 2;
    private static final int CENTER_THRESHOLD_PX = 20;
    private static final int LOCK_CONFIRMATION_FRAMES = 3;
    private static final double SHOOTER_POWER = 1.0;
    private static final int POLL_MAX_PACKETS = 2;
    private static final long POLL_TIME_BUDGET_MS = 4;

    private HuskyLens2 huskyLens;
    private Servo turretServo;
    private DcMotor shooterMotor;

    @Override
    public void runOpMode() {
        huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");
        turretServo = hardwareMap.get(Servo.class, "turretServo");
        shooterMotor = hardwareMap.get(DcMotor.class, "shooterMotor");

        double servoPosition = SERVO_CENTER;
        int lockedFrames = 0;

        turretServo.setPosition(servoPosition);
        shooterMotor.setPower(0.0);

        telemetry.addData("Status", "Connecting to HuskyLens Gen2...");
        telemetry.update();

        if (!huskyLens.knock()) {
            telemetry.addData("Error", "HuskyLens Gen2 is not responding on I2C.");
            telemetry.update();
            return;
        }

        if (!huskyLens.selectAlgorithm(Algorithm.ALGORITHM_TAG_RECOGNITION)) {
            telemetry.addData("Error", "Failed to switch HuskyLens to Tag Recognition.");
            telemetry.update();
            return;
        }

        telemetry.addData("Status", "Ready for AprilTag tracking");
        telemetry.addData("Camera Center X", CAMERA_CENTER_X);
        telemetry.update();

        waitForStart();
        boolean requestStarted = huskyLens.beginResultRequest(Algorithm.ALGORITHM_TAG_RECOGNITION);

        while (opModeIsActive()) {
            if (!requestStarted) {
                requestStarted = huskyLens.beginResultRequest(Algorithm.ALGORITHM_TAG_RECOGNITION);
            }

            boolean complete = huskyLens.pollResultRequest(POLL_MAX_PACKETS, POLL_TIME_BUDGET_MS);
            ReadStatus status = huskyLens.getLastReadStatus();
            List<Block> tags = huskyLens.getCachedBlocks();
            Block targetTag = chooseClosestToCenter(tags);

            if (targetTag == null) {
                lockedFrames = 0;
                shooterMotor.setPower(0.0);
                telemetry.addData("Mode", "Searching");
            } else {
                int errorX = targetTag.xCenter - CAMERA_CENTER_X;
                telemetry.addData("Tag ID", targetTag.id);
                telemetry.addData("Tag Name", emptyAsDash(targetTag.name));
                telemetry.addData("Tag Content", emptyAsDash(targetTag.content));
                telemetry.addData("Tag Center", "%d, %d", targetTag.xCenter, targetTag.yCenter);
                telemetry.addData("Tag Size", "%d x %d", targetTag.width, targetTag.height);
                telemetry.addData("Error X", errorX);

                if (Math.abs(errorX) > CENTER_THRESHOLD_PX) {
                    lockedFrames = 0;
                    servoPosition = clip(
                            servoPosition + (PAN_ERROR_TO_SERVO_SIGN * Math.signum(errorX) * SERVO_STEP),
                            SERVO_MIN,
                            SERVO_MAX
                    );
                    turretServo.setPosition(servoPosition);
                    shooterMotor.setPower(0.0);
                    telemetry.addData("Mode", "Centering");
                } else {
                    lockedFrames++;
                    if (lockedFrames >= LOCK_CONFIRMATION_FRAMES) {
                        shooterMotor.setPower(SHOOTER_POWER);
                        telemetry.addData("Mode", "Locked and shooting");
                    } else {
                        shooterMotor.setPower(0.0);
                        telemetry.addData("Mode", "Locking");
                    }
                }
            }

            telemetry.addData("Detected Tags", tags.size());
            telemetry.addData("Locked Frames", lockedFrames);
            telemetry.addData("Servo Position", "%.3f", servoPosition);
            telemetry.addData("Threshold Px", CENTER_THRESHOLD_PX);
            telemetry.addData("Read Status", status);
            telemetry.update();

            if (complete || isFinishedStatus(status)) {
                requestStarted = huskyLens.beginResultRequest(Algorithm.ALGORITHM_TAG_RECOGNITION);
            }

            idle();
        }

        shooterMotor.setPower(0.0);
        huskyLens.exit();
    }

    private Block chooseClosestToCenter(List<Block> tags) {
        Block best = null;
        int bestDistance = Integer.MAX_VALUE;
        int bestArea = -1;

        for (Block tag : tags) {
            int distance = Math.abs(tag.xCenter - CAMERA_CENTER_X);
            int area = tag.width * tag.height;
            if (best == null || distance < bestDistance || (distance == bestDistance && area > bestArea)) {
                best = tag;
                bestDistance = distance;
                bestArea = area;
            }
        }
        return best;
    }

    private double clip(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String emptyAsDash(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    private boolean isFinishedStatus(ReadStatus status) {
        return status == ReadStatus.TIMEOUT
                || status == ReadStatus.TRUNCATED
                || status == ReadStatus.CHECKSUM_ERROR
                || status == ReadStatus.MALFORMED_PACKET
                || status == ReadStatus.PACKET_TOO_LARGE
                || status == ReadStatus.I2C_ERROR;
    }
}
