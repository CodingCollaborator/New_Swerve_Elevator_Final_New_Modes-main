// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import edu.wpi.first.wpilibj.Encoder;
import autos.AutoRoutines;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.CoralIntake;
import frc.robot.subsystems.Elevator;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class RobotContainer {

    private final Telemetry logger = new Telemetry(Constants.MaxSpeed);

    private final CommandXboxController joystick0 = new CommandXboxController(0); //#This Joystick Controls The Driving(Will Also Control Elevator)
    private final CommandXboxController joystick1 = new CommandXboxController(1);//#This Joystick Controls The Intake System


    private int alliance; //for Auto

    public RobotContainer(int all) //Is there a way to set this at the drive station.
    {
        configureBindings(all);
    }

    private void configureBindings(int all) {
        this.alliance = all;
        // Note that X is defined as forward according to WPILib convention,
        // and Y is defined as to the left according to WPILib convention.
        Constants.drivetrain.setDefaultCommand(
            // Drivetrain will execute this command periodically
            Constants.drivetrain.applyRequest(() ->
                Constants.drive.withVelocityX(-joystick0.getLeftY() * Constants.MaxSpeed * Constants.getSpeedFactor() * Math.pow(-1, alliance)) // Drive forward with negative Y (forward) #SpeedHalved
                    .withVelocityY(-joystick0.getLeftX() * Constants.MaxSpeed * Constants.getSpeedFactor() * Math.pow(-1, alliance)) // Drive left with negative X (left) #SpeedHalved
                    .withRotationalRate(-joystick0.getRightX() * Constants.MaxAngularRate * 0.5) // Drive counterclockwise with negative X (left) #SpeedHalved
            )                                                                              //Change if Possible(1.00)
            //# When Command Is Executed, Request Is Applied Based on What Joystick Buttons Are Being Pressed

            //VERY IMPORTANT, REMEMBER TO SET TO FULL SPEED FOR COMPETITION.

        );
        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        //DRIVE SYSTEM CONTROLS
        joystick0.back().and(joystick0.y()).whileTrue(Constants.drivetrain.sysIdDynamic(Direction.kForward));
        joystick0.back().and(joystick0.x()).whileTrue(Constants.drivetrain.sysIdDynamic(Direction.kReverse));
        joystick0.start().and(joystick0.y()).whileTrue(Constants.drivetrain.sysIdQuasistatic(Direction.kForward));
        joystick0.start().and(joystick0.x()).whileTrue(Constants.drivetrain.sysIdQuasistatic(Direction.kReverse));
        joystick0.leftBumper().onTrue(Constants.drivetrain.runOnce(() -> Constants.drivetrain.seedFieldCentric()));
        joystick0.povUp().onTrue(Constants.switchToTurboSpeed());
        joystick0.povDown().onTrue(Constants.switchToRegularSpeed());
        //Drive System Controls

        //Climber Controls
        //joystick0.a().onTrue(Constants.climber.letOut());
        //joystick0.b().onTrue(Constants.climber.pullIn());
        //Climber Controls

        //CORAL MODE CONTROLS
        joystick1.a().onTrue(Constants.elevator.setCoralHeight(2.5, Constants.getMode()));             //  LevelOne/Reset/!Feeder!
        joystick1.a().onTrue(Constants.flipper.setDesiredCoralAngle(7.52, Constants.getMode()));//FEEDER ANGLE - TUNE

        joystick1.x().onTrue(Constants.elevator.setCoralHeight(41.1, Constants.getMode()));            //  LevelTwo
        joystick1.x().onTrue(Constants.flipper.setDesiredCoralAngle(2.357, Constants.getMode()));//LEVEL TWO ANGLE - TUNE

        joystick1.y().onTrue(Constants.elevator.setCoralHeight(128.63, Constants.getMode()));        //  LevelThree
        joystick1.y().onTrue(Constants.flipper.setDesiredCoralAngle(2.357, Constants.getMode()));//SAME AS LAST ANGLE

        joystick1.b().onTrue(Constants.elevator.setCoralHeight(230, Constants.getMode()));           //  LevelFour
        joystick1.b().onTrue(Constants.flipper.setDesiredCoralAngle(0, Constants.getMode()));//LEVEL FOUR ANGLE - TUNE

        joystick1.rightStick().onTrue(Constants.flipper.setDesiredCoralAngle(0, Constants.getMode()));//TROUGH ANGLE - TUNE

        joystick1.leftBumper().whileTrue(Constants.coralIntake.pullCoralIn(Constants.getMode())).onFalse(Constants.coralIntake.hold(Constants.getMode()));//On true, button pressed, calls method.
        joystick1.rightBumper().whileTrue(Constants.coralIntake.pushCoralOut(Constants.getMode())).onFalse(Constants.coralIntake.stopIntake());
        //CORAL MODE CONTROLS
       
        //SWITCH MODES
        joystick1.povUp().onTrue(Constants.switchToAlgaeMode());
        joystick1.povDown().onTrue(Constants.switchToCoralMode());
        //SWITCH MODES

        //ALGAE MODE CONTROLS
        joystick1.a().onTrue(Constants.elevator.setAlgaeHeight(0, Constants.getMode()));//PROCESSOR HEIGHT - TUNE
        joystick1.a().onTrue(Constants.flipper.setDesiredAlgaeAngle(0, Constants.getMode()));//PROCESSOR ANGLE - TUNE

        joystick1.x().onTrue(Constants.elevator.setAlgaeHeight(50, Constants.getMode()));//  LEVEL TWO HEIGHT - TUNE
        joystick1.x().onTrue(Constants.flipper.setDesiredAlgaeAngle(0, Constants.getMode()));//LEVEL TWO ANGLE - TUNE

        joystick1.y().onTrue(Constants.elevator.setAlgaeHeight(144.63, Constants.getMode()));//  LEVEL THREE HEIGHT - TUNE
        joystick1.y().onTrue(Constants.flipper.setDesiredAlgaeAngle(0, Constants.getMode()));////LEVEL THREE ANGLE - TUNE

        joystick1.b().onTrue(Constants.elevator.setCoralHeight(230, Constants.getMode()));//BARGE SHOT HEIGHT - TUNE
        joystick1.b().onTrue(Constants.flipper.setDesiredAlgaeAngle(0, Constants.getMode()));//BARGE SHOT ANGLE - TUNE

        joystick1.leftBumper().whileTrue(Constants.algaeIntake.pullAlgaeIn(Constants.getMode())).onFalse(Constants.algaeIntake.hold(Constants.getMode()));//On true, button pressed, calls method.
        joystick1.rightBumper().whileTrue(Constants.algaeIntake.pushAlgaeOut(Constants.getMode())).onFalse(Constants.algaeIntake.stopIntake());
        //ALGAE MODE CONTROLS


        Constants.drivetrain.registerTelemetry(logger::telemeterize);
    }

    public Command getAutonomousCommand() 
    {
        return AutoRoutines.Taxi.routine(Constants.drivetrain, alliance);
    }
}

//OBSOLETE BUTTON BINDINGS

 //joystick0.a().whileTrue(drivetrain.applyRequest(() -> brake));
        //#Constructs a Trigger around Button A that requests the DriveTrain Break
        //joystick0.b().whileTrue(drivetrain.applyRequest(() ->
        //point.withModuleDirection(new Rotation2d(-joystick0.getLeftY(), -joystick0.getLeftX()))
        //));
         //#Creates a Trigger around Button B that Points Wheels in Direction of Joystick - (Not Sure of Functionality)
//joystick0.a().onTrue(elevator.setToLevelOne()).onFalse(elevator.stopElevator());       
        //joystick0.b().onTrue(elevator.setToLevelTwo()).onFalse(elevator.stopElevator());        
        //joystick0.y().onTrue(elevator.setToLevelThree());       
        //joystick0.x().onTrue(elevator.setToLevelFour());      
        //joystick0.rightBumper().onTrue(elevator.setToLevelZero());  
        //joystick0.x().onTrue(elevator.moveDown()).onFalse(elevator.stopElevator()); 
        //joystick0.y().onTrue(elevator.moveUp()).onFalse(elevator.stopElevator()); 