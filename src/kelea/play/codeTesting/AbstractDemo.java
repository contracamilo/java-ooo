package kelea.play.codeTesting;

public class AbstractDemo {

    static abstract class Notification {
        private final String employee;

        Notification(String employee) {
            this.employee = employee;
        }

        // Método normal: lo comparten todas las hijas
        void send() {
            System.out.println("Para " + employee + ": " + message());
        }

        // Método abstracto: cada hija decide su texto
        abstract String message();
    }

    static class ShiftPublished extends Notification {
        ShiftPublished(String employee) { super(employee); }

        @Override
        String message() { return "tu horario de la semana ya está publicado"; }
    }

    static class ShiftChanged extends Notification {
        ShiftChanged(String employee) { super(employee); }

        @Override
        String message() { return "han cambiado tu turno del lunes"; }
    }

    public static void main(String[] args) {
        new ShiftPublished("ana").send();   // Para ana: tu horario de la semana ya está publicado
        new ShiftChanged("luis").send();    // Para luis: han cambiado tu turno del lunes
        // new Notification("marta");       ❌ no compila: es abstracta
    }
}