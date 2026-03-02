package splendor.app;
import java.util.EnumMap;
import splendor.entities.*;

public class Main {
    public static void main(String[] args) {
        //creates player object
        Player p1 = new Player("Danial");
        System.out.println(p1);
        System.out.println();

        //create one noble
        EnumMap<GemColor, Integer> req = new EnumMap<>(GemColor.class);
        req.put(GemColor.RUBY, 3);
        req.put(GemColor.EMERALD, 3);
        req.put(GemColor.ONYX, 3);
        Noble n1 = new Noble("N1", 3, req);
        System.out.println(n1);

        System.out.println();

        //create the gem bank
        GemBank gemBank = new GemBank(3);
        System.out.println(gemBank);
    }
}
