import lexico.Classe;
import lexico.Lexico;
import lexico.Token;

public class App {
    public static void main(String[] args) throws Exception {
        Lexico lexico = new Lexico("programaPascal.pas");
        Token token;
        do{
            token = lexico.nextToken();
            System.out.println(token);
        }while(token.getClasse() != Classe.EOF);
    }
}
