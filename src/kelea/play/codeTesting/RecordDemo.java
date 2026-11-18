package kelea.play.codeTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class RecordDemo {

    record TaskBlock(String taskName, int startMinute, int endMinute) {
        TaskBlock {   // constructor compacto: valida antes de crear el objeto
            if (endMinute <= startMinute) {
                throw new IllegalArgumentException("end must be after start");
            }
        }

        int duration() {   // puedes añadir métodos
            return endMinute - startMinute;
        }
    }

    record Shift(String employee, List<TaskBlock> blocks) {
        Shift {
            blocks = List.copyOf(blocks);   // copia defensiva
        }
    }

    public static void main(String[] args) {
        TaskBlock b1 = new TaskBlock("Caja", 540, 780);
        TaskBlock b2 = new TaskBlock("Caja", 540, 780);

        System.out.println(b1);            // TaskBlock[taskName=Caja, startMinute=540, endMinute=780]
        System.out.println(b1.equals(b2)); // true: mismo contenido
        System.out.println(b1 == b2);      // false: objetos distintos
        System.out.println(b1.duration()); // 240

        List<TaskBlock> blocks = new ArrayList<>();
        blocks.add(b1);
        Shift shift = new Shift("ana", blocks);
        blocks.add(new TaskBlock("Pausa", 780, 810)); // modifico la lista original
        System.out.println(shift.blocks().size());    // 1: el turno no se ha enterado


        // NUEVO: al final del main
        Set<TaskBlock> set = new HashSet<>();
        System.out.println(set.add(b1));   // true
        System.out.println(set.add(b2));   // false
        System.out.println(set.size());    // 1
    }
}