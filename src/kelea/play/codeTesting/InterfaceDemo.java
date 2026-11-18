package kelea.play.codeTesting;

import java.util.ArrayList;
import java.util.List;

public class InterfaceDemo {

    record Shift(String employee, String day) {}

    // EL CONTRATO: qué se puede hacer con los turnos guardados
    interface ShiftRepository {
        void save(Shift shift);
        List<Shift> findByEmployee(String employee);
    }

    // UNA IMPLEMENTACIÓN: guarda en memoria
    static class InMemoryShiftRepository implements ShiftRepository {
        private final List<Shift> shifts = new ArrayList<>();

        @Override
        public void save(Shift shift) {
            shifts.add(shift);
        }

        @Override
        public List<Shift> findByEmployee(String employee) {
            return shifts.stream()
                    .filter(shift -> shift.employee().equals(employee))
                    .toList();
        }
    }

    // QUIEN LO USA: solo conoce el contrato, no la implementación
    static class PlanShiftService {
        private final ShiftRepository repository;

        PlanShiftService(ShiftRepository repository) {
            this.repository = repository;
        }

        void plan(String employee, String day) {
            repository.save(new Shift(employee, day));
        }
    }

    public static void main(String[] args) {
        ShiftRepository repository = new InMemoryShiftRepository();
        PlanShiftService service = new PlanShiftService(repository);

        service.plan("ana", "MONDAY");
        System.out.println(repository.findByEmployee("ana"));
        // [Shift[employee=ana, day=MONDAY]]
    }
}