package lexico;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class Lexico {
    private String nomeArquivo;
    private Token token;
    private BufferedReader br;
    private char caractere;
    private StringBuilder lexema = new StringBuilder();

    public Lexico(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        try {
            BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            this.br = br;
            caractere = proximoChar();
        } catch (IOException e) {
            System.err.println("Não foi possível abrir o arquivo: " + nomeArquivo);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private char proximoChar() {
        try {
            return (char) br.read();
        } catch (IOException e) {
            System.err.println("Não foi possível ler do arquivo: " + nomeArquivo);
            e.printStackTrace();
            System.exit(-1);
        }
        return 0;
    }

    public Token nextToken(){
        lexema.setLength(0);

        if(Character.isDigit(caractere)){
            while(Character.isDigit(caractere)){
                lexema.append(caractere);
                caractere = proximoChar();
            }
        }

        token = new Token();
        token.setClasse(Classe.numeroInteiro);
        token.setValor(new Valor(Integer.parseInt(lexema.toString())));

        return token;
    }
}
