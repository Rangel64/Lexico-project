package lexico;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Lexico {
    private String nomeArquivo;
    private Token token;
    private BufferedReader br;
    private char caractere;
    private StringBuilder lexema = new StringBuilder();
    private int linha;
    private int coluna;

    private List<String> palavrasReservadas = Arrays.asList(
        "and", "array", "begin", "case", "const",
        "div", "do", "downto", "else", "end",
        "file", "for", "function", "goto", "if",
        "in", "label", "mod", "nil", "not",
        "of", "or", "packed", "procedure", "program",
        "record", "repeat", "set", "then", "to",
        "type", "until", "var", "while", "with",
        "integer", "real", "boolean", "char", "string",
        "write", "writeln", "read");

    public Lexico(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        try {
            BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            this.br = br;
            caractere = proximoChar();
            linha = 1;
            coluna = 0;
        } catch (IOException e) {
            System.err.println("Não foi possível abrir o arquivo: " + nomeArquivo);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private char proximoChar() {
        try {
            coluna++;
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

        do{
            if(caractere==' ' || caractere == '\t'){
                while(caractere==' ' || caractere == '\t'){
                    if(caractere == '\t'){
                        coluna = coluna + 3;
                    }
                    caractere = proximoChar();
                }
                
            }

            else if(caractere=='\n'){
                while(caractere=='\n'){
                    linha++;
                    coluna = 0;
                    caractere = proximoChar();
                }
            }

            else if(Character.isDigit(caractere)){
                token = new Token(linha, coluna);
                while(Character.isDigit(caractere)){
                    lexema.append(caractere);
                    caractere = proximoChar();
                }

                token.setClasse(Classe.numeroInteiro);
                token.setValor(new Valor(Integer.parseInt(lexema.toString())));
                return token;
            }
            
            else if(Character.isAlphabetic(caractere)){
                token = new Token(linha, coluna);
                while (Character.isAlphabetic(caractere)||Character.isDigit(caractere)) {
                    lexema.append(caractere);
                    caractere = proximoChar();   
                }

                token.setClasse(Classe.indentificador);
                token.setValor(new Valor(lexema.toString()));

                if(palavrasReservadas.contains(lexema.toString().toLowerCase())){
                    token.setClasse(Classe.palavraReservada);
                }

                return token;
            }
            
            else if(caractere==65535){
                token = new Token(linha, coluna);
                token.setClasse(Classe.EOF);
                return token;
            }

            else if(caractere=='+'){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.operadorSoma);
                return token;
            }

            else if(caractere=='-'){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.operadorSubtracao);
                return token;
            }

            else if(caractere=='*'){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.operadorMultiplicacao);
                return token;
            }

            else if(caractere=='/'){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.operadorDivisao);
                return token;
            }

            else if(caractere==':'){
                token = new Token(linha, coluna);
                token.setClasse(Classe.doisPontos);
                caractere = proximoChar();
                if(caractere=='='){
                    caractere = proximoChar();
                    token.setClasse(Classe.doisPontosIgual);
                    return token;
                }else{
                    caractere = proximoChar();
                    return token;
                }
            }

            else if(caractere=='>'){
                token = new Token(linha, coluna);
                token.setClasse(Classe.operadorMaior);
                caractere = proximoChar();
                if(caractere=='='){
                    caractere = proximoChar();
                    token.setClasse(Classe.operadorMaiorIgual);
                    return token;
                }else{
                    caractere = proximoChar();
                    return token;
                }
            }

            else if(caractere=='<'){
                token = new Token(linha, coluna);
                token.setClasse(Classe.operadorMenor);
                caractere = proximoChar();
                if(caractere=='='){
                    caractere = proximoChar();
                    token.setClasse(Classe.operadorMenorIgual);
                    return token;
                }
                else if(caractere=='>'){
                    caractere = proximoChar();
                    token.setClasse(Classe.operadorDiferente);
                    return token;
                }else{
                    caractere = proximoChar();
                    return token;
                }
            }

            else if(caractere==';'){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.pontoEVirgula);
                return token;
            }

            else if(caractere==','){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.virgula);
                return token;
            }

            else if(caractere=='.'){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.ponto);
                return token;
            }

            else if(caractere=='('){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.parentesesEsquerdo);
                return token;
            }

            else if(caractere==')'){
                caractere = proximoChar();
                token = new Token(linha, coluna);
                token.setClasse(Classe.parentesesDireito);
                return token;
            }


        }while(caractere!=65535);

        token = new Token(linha, coluna);
        token.setClasse(Classe.EOF);

        return token;  
        
    }
}
