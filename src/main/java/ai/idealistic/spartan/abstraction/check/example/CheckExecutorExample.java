package ai.idealistic.spartan.abstraction.check.example;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckEnums;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;

public class CheckExecutorExample extends CheckRunner {

    private final DetectionExecutorExample detection1;
    private final CheckDetection detection2;

    public CheckExecutorExample(CheckEnums.HackType hackType, PlayerProtocol protocol, String playerName) {
        super(hackType, protocol);

        this.detection1 = new DetectionExecutorExample(
                this
        );


        this.detection2 = new ImplementedDetection(
                this,
                Check.DataType.JAVA,
                Check.DetectionType.PACKETS,
                "detection_option_name_in_checks_yml",
                true // Enabled By Default Or Not
        );

        // This is the constructor you will call to initiate this abstract class
        // implementation. If your check/detection has higher complexity, it will
        // likely need to be produced in multiple classes. In that case, you can
        // separate the functionality by using the 'DetectionExecutor' class and
        // connect them all via the 'CheckExecutor' class.
    }

    @Override
    protected void handleInternal(boolean cancelled, Object object) {
        // This method should be used to handle data for a check/detection.
        //
        // The boolean 'cancelled' is 'true' when an event is cancelled by the server
        // or by another plugin. Based on configuration, a user of this plugin may
        // choose for cancelled events to not go through, thus causing this method to
        // not be called at all.
    }

    @Override
    protected boolean canRun() {
        this.detection1.customMethod2();
        // This method should be used to judge whether a check should run or not.
        // However, each check/detection may have different requirements, so use
        // this method for the requirements all checks/detections have in common.
        // Keep in mind that basic factors such as the check being enabled are
        // already accounted for prior to running this method.
        return true;
    }

    // Here you can add more methods since you are extending an abstract class.
    // It is nonetheless recommended to stick to the default methods, otherwise
    // you may run into scenarios where you need to use casting to access methods
    // of the child class from the parent class which produces overhead. For
    // comparison, accessing a parent class from a child class is significantly
    // lighter.
}
