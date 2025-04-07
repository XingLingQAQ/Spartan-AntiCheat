package ai.idealistic.spartan.abstraction.check.definition;

import ai.idealistic.spartan.abstraction.check.Check;
import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;

public class ImplementedDetection extends CheckDetection {

    public ImplementedDetection(
            CheckRunner executor,
            Check.DataType forcedDataType,
            Check.DetectionType detectionType,
            String name,
            Boolean def
    ) {
        super(
                executor,
                forcedDataType,
                detectionType,
                name,
                def
        );
    }

    public ImplementedDetection(
            CheckRunner executor,
            Check.DataType forcedDataType,
            Check.DetectionType detectionType,
            String name,
            Boolean def,
            long defaultAverageTime,
            long timeToNotify,
            long timeToPrevent,
            long timeToPunish
    ) {
        super(
                executor,
                forcedDataType,
                detectionType,
                name,
                def,
                defaultAverageTime,
                timeToNotify,
                timeToPrevent,
                timeToPunish
        );
    }

    public ImplementedDetection(
            CheckDetection detection,
            String name,
            Boolean def
    ) {
        super(
                detection.executor,
                detection.forcedDataType,
                detection.detectionType,
                name,
                def,
                detection.defaultAverageTime,
                detection.timeToNotify,
                detection.timeToPrevent,
                detection.timeToPunish
        );

        if (!detection.hasName) {
            detection.executor.removeDetection(detection);
            detection.executor.hackType.removeDetection(detection.name);
        }
    }

    public ImplementedDetection(
            CheckDetection detection,
            String name,
            Boolean def,
            long defaultAverageTime,
            long timeToNotify,
            long timeToPrevent,
            long timeToPunish
    ) {
        super(
                detection.executor,
                detection.forcedDataType,
                detection.detectionType,
                name,
                def,
                defaultAverageTime,
                timeToNotify,
                timeToPrevent,
                timeToPunish
        );

        if (!detection.hasName) {
            detection.executor.removeDetection(detection);
            detection.executor.hackType.removeDetection(detection.name);
        }
    }

}
