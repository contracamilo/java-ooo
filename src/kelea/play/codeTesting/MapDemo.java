package kelea.play.codeTesting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapDemo {

    enum TaskColor { BLUE, GREEN, GRAY }

    record Task(String name, TaskColor color, boolean isBreak, int minutes) {}

    public static void main(String[] args) {
        List<Task> tasks = List.of(
                new Task("Caja", TaskColor.BLUE, false, 240),
                new Task("Comida", TaskColor.GRAY, true, 30),
                new Task("Reposición", TaskColor.BLUE, false, 180),
                new Task("Descanso", TaskColor.GRAY, true, 15)
        );



        Map<TaskColor, Integer> countByColor = new HashMap<>();

        for (Task task : tasks) {
            System.out.println(task.name);
            countByColor.merge(task.color(), 1, Integer::sum);
        }

        System.out.println(countByColor);                                 // {BLUE=2, GRAY=2}
        System.out.println(countByColor.get(TaskColor.BLUE));             // 2
        System.out.println(countByColor.get(TaskColor.GREEN));            // null ⚠️
        System.out.println(countByColor.getOrDefault(TaskColor.GREEN, 0)); // 0
    }
}