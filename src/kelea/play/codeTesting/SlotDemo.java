package kelea.play.codeTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SlotDemo {

    static class Slot {
        private final int hour;

        Slot(int hour) {
            this.hour = hour;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;                    // 1. ¿es el mismo objeto?
            if (!(o instanceof Slot other)) return false;  // 2. ¿es un Slot?
            return hour == other.hour;                     // 3. ¿mismos datos?
        }

        @Override
        public int hashCode() {
            return Objects.hash(hour);   // los MISMOS campos que en equals
        }
    }

    public static void main(String[] args) {
        Map<Slot, Integer> coverage = new HashMap<>();
        coverage.put(new Slot(9), 3);
        System.out.println(coverage.get(new Slot(9)));   // 3 ✅
    }
}