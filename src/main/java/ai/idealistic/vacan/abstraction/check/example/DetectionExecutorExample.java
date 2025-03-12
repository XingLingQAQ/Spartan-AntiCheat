package ai.idealistic.vacan.abstraction.check.example;

import ai.idealistic.vacan.abstraction.check.CheckDetection;
import ai.idealistic.vacan.abstraction.check.CheckRunner;

public class DetectionExecutorExample extends CheckDetection {

    DetectionExecutorExample(CheckRunner executor) {
        super(
                executor,
                null,
                null,
                "detection_option_name_in_checks_yml",
                true // Enabled By Default Or Not
        );
    }

    void customMethod1() {

    }

    boolean customMethod2() {
        return true;
    }

}
