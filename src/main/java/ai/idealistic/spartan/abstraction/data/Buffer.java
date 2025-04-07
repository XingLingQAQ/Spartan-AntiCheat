package ai.idealistic.spartan.abstraction.data;

import ai.idealistic.spartan.functionality.server.TPS;
import ai.idealistic.spartan.utils.math.AlgebraUtils;

import java.util.Map;

public class Buffer {

    private final Map<String, IndividualBuffer> storage;

    public static class IndividualSimpleBuffer {

        public int count;

        public IndividualSimpleBuffer() {
        }

    }

    public static class IndividualBuffer {

        private double count;
        private long start;

        public IndividualBuffer() {
            this.reset();
        }

        public long ticksPassed() {
            return AlgebraUtils.integerCeil((System.currentTimeMillis() - this.start) / (double) TPS.tickTime);
        }

        public long ticksRemaining(int maxTicks) {
            return Math.max(0, maxTicks - this.ticksPassed());
        }

        public double set(double amount) {
            return this.count = amount;
        }

        public double increase(double amount) {
            this.count += amount;
            return this.count;
        }

        public double decrease(double amount) {
            if (this.count > amount) {
                this.count -= amount;
                return this.count;
            } else {
                this.count = 0;
                return 0;
            }
        }

        public void reset() {
            this.count = 0;
            this.start = System.currentTimeMillis();
        }

        public double count(double amount, int maxTicks) {
            if (this.ticksPassed() > maxTicks) {
                this.reset();
            }
            return this.increase(amount);
        }

    }

    public Buffer(Map<String, IndividualBuffer> map) {
        this.storage = map;
    }

    public double get(String name) {
        IndividualBuffer obj = storage.get(name);
        return obj != null ? obj.count : 0;
    }

    public void set(String name, int amount) {
        IndividualBuffer obj = storage.get(name);

        if (obj == null) {
            obj = new IndividualBuffer();
            storage.put(name, obj);
        }
        obj.reset();
        obj.increase(amount);
    }

    public double increase(String name, int amount) {
        return storage.computeIfAbsent(
                name,
                k -> new IndividualBuffer()
        ).increase(amount);
    }

    public double decrease(String name, int amount) {
        IndividualBuffer obj = storage.get(name);
        return obj != null ? obj.decrease(amount) : 0;
    }

    public int getRemainingTicks(String name, int maxTicks) {
        IndividualBuffer obj = storage.get(name);

        if (obj == null) {
            return 0;
        } else {
            long ticksPassed = obj.ticksPassed();
            return ticksPassed > maxTicks ? 0 : (int) (maxTicks - ticksPassed);
        }
    }

    public double count(String name, int maxTicks) {
        return storage.computeIfAbsent(
                name,
                k -> new IndividualBuffer()
        ).count(1, maxTicks);
    }

    public double ratio(String name, int minimumTicks, int maxTicks) {
        IndividualBuffer obj = storage.computeIfAbsent(name, k -> new IndividualBuffer());
        double ticksPassed = obj.ticksPassed();
        if (ticksPassed > maxTicks) obj.reset();
        double count = obj.increase(1);
        return count >= minimumTicks ? count / ticksPassed : 0.0;
    }

    public boolean canDo(String name) {
        return get(name) >= 0;
    }

    public void remove(String name) {
        storage.remove(name);
    }

    public void remove(String[] names) {
        for (String name : names) {
            storage.remove(name);
        }
    }

}
