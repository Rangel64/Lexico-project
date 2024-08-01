import lexico.Lexico;

public class App {
    public static void main(String[] args) throws Exception {
        Lexico lexico = new Lexico("programaPascal.pas");
        System.out.println(lexico.nextToken());
    }
}
