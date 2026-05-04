package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2;
import org.firstinspires.ftc.teamcode.huskylens.HuskyLens2.*;

import java.util.List;

/**
 * HuskyLensTagTracker demonstrates a practical use case:
 * 1. Recognizes AprilTags (Tag Recognition).
 * 2. Uses a Servo to rotate the camera/mechanism to center the Tag.
 * 3. Once centered, activates a motor to "shoot".
 */
@TeleOp(name = "HuskyLens: Tag Tracker & Shooter", group = "Practical")
public class HuskyLens2TagTracker extends LinearOpMode {

    // Hardware components
    private HuskyLens2 huskyLens;
    private Servo turretServo;
    private DcMotor shooterMotor;

    // Configuration constants
    private static final double SERVO_CENTER = 0.5;
    private static final double SERVO_STEP = 0.005; // Adjust for speed of tracking
    private static final int HUSKY_CENTER_X = 160;   // HuskyLens Gen2 default center is 160 (320/2)
    private static final int CENTER_THRESHOLD = 10;  // Allowed error in pixels

    @Override
    public void runOpMode() {
        // Initialize Hardware
        huskyLens = hardwareMap.get(HuskyLens2.class, "huskylens");
        turretServo = hardwareMap.get(Servo.class, "turretServo");
        shooterMotor = hardwareMap.get(DcMotor.class, "shooterMotor");

        double servoPosition = SERVO_CENTER;
        turretServo.setPosition(servoPosition);

        telemetry.addData("Status", "Initializing HuskyLens...");
        telemetry.update();

        // 1. Setup HuskyLens for Tag Recognition
        if (!huskyLens.knock()) {
            telemetry.addData("Error", "HuskyLens disconnected!");
            telemetry.update();
        }
        huskyLens.selectAlgorithm(Algorithm.ALGORITHM_TAG_RECOGNITION);

        telemetry.addData("Status", "Ready. Tracking AprilTags...");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // 2. Request Tag detections
            List<Block> tags = huskyLens.requestBlocks(Algorithm.ALGORITHM_TAG_RECOGNITION);
            
            if (!tags.isEmpty()) {
                // Focus on the first detected tag
                Block targetTag = tags.get(0);
                int errorX = targetTag.xCenter - HUSKY_CENTER_X;

                telemetry.addData("Tag ID", targetTag.id);
                telemetry.addData("Error X", errorX);

                // 3. Rotate Servo to center the Tag
                if (Math.abs(errorX) > CENTER_THRESHOLD) {
                    // If tag is to the right (error > 0), decrease servo pos to turn left (depending on mounting)
                    // If tag is to the left (error < 0), increase servo pos to turn right
                    if (errorX > 0) {
                        servoPosition -= SERVO_STEP;
                    } else {
                        servoPosition += SERVO_STEP;
                    }
                    
                    // Clamp servo position between 0 and 1
                    servoPosition = Math.max(0, Math.min(1, servoPosition));
                    turretServo.setPosition(servoPosition);
                    
                    shooterMotor.setPower(0); // Don't shoot while moving
                    telemetry.addData("Mode", "Centering...");
                } else {
                    // 4. Centered! Turn on the motor to "shoot"
                    telemetry.addData("Mode", "LOCKED! SHOOTING!");
                    shooterMotor.setPower(1.0);
                    // Optional: Take a photo of the "hit"
                    // huskyLens.takePhoto(HuskyLens2.RESOLUTION_DEFAULT);
                }
            } else {
                telemetry.addData("Mode", "Searching for Tag...");
                shooterMotor.setPower(0);
            }

            telemetry.addData("Servo Pos", "%.3f", servoPosition);
            telemetry.update();
            
            // Short delay to prevent overwhelming the I2C bus
            sleep(20);
        }
        
        // Cleanup
        shooterMotor.setPower(0);
        huskyLens.exit();
    }
}
