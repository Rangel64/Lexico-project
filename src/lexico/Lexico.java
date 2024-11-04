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
    private TabelaSimbolos tabelaSimbolos;

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
        linha = 1;
        coluna = 0;
        this.nomeArquivo = nomeArquivo;
        String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();
        tabelaSimbolos = new TabelaSimbolos();
        try {
            br = new BufferedReader(new FileReader(caminhoArquivo, StandardCharsets.UTF_8));
            caractere = proximoChar();
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

    public Token nextToken() {
        lexema.setLength(0);
        while (true) {
            // Tratamento de espaços em branco e tabulações
            if (caractere == ' ' || caractere == '\t') {
                while (caractere == ' ' || caractere == '\t') {
                    caractere = proximoChar();
                }
            }

            // Tratamento de quebra de linha
            else if(caractere == '\n' || caractere == '\r') {
                while(caractere == '\n' || caractere == '\r') {
                    if (caractere == '\n') {
                        linha++;
                        coluna = 0;
                    }
                    caractere = proximoChar();
                }
            }
            
            // Tratamento de números inteiros
            else if (Character.isDigit(caractere)) {
                token = new Token(linha, coluna);
                while (Character.isDigit(caractere)) {
                    lexema.append(caractere);
                    caractere = proximoChar();
                }
                token.setClasse(Classe.numeroInteiro);
                token.setValor(new Valor(Integer.parseInt(lexema.toString())));
                // System.out.println(token.toString());
                return token;
            }

            // Tratamento de identificadores e palavras reservadas
            else if (Character.isAlphabetic(caractere)) {
                token = new Token(linha, coluna);
                while (Character.isAlphabetic(caractere) || Character.isDigit(caractere)) {
                    lexema.append(caractere);
                    caractere = proximoChar();
                }
                String lexemaStr = lexema.toString();
                token.setValor(new Valor(lexemaStr));

                if (palavrasReservadas.contains(lexemaStr.toLowerCase())) {

                    token.setClasse(Classe.palavraReservada);
                } else {
                    token.setClasse(Classe.identificador);
                    tabelaSimbolos.add(lexemaStr);
                }
                // System.out.println(token.toString());
                return token;
            }

            // Fim de arquivo
            else if (caractere == 65535) {
                token = new Token(linha, coluna);
                token.setClasse(Classe.EOF);
                // System.out.println(token.toString());
                return token;
            }

            else {
                // Tratamento de operadores e símbolos especiais
                token = new Token(linha, coluna);
                switch (caractere) {
                    case '+':
                        caractere = proximoChar();
                        token.setClasse(Classe.operadorSoma);
                        // System.out.println(token.toString());
                        return token;
                    case '-':
                        caractere = proximoChar();
                        token.setClasse(Classe.operadorSubtracao);
                        // System.out.println(token.toString());
                        return token;
                    case '*':
                        caractere = proximoChar();
                        token.setClasse(Classe.operadorMultiplicacao);
                        // System.out.println(token.toString());
                        return token;
                    case '/':
                        caractere = proximoChar();
                        token.setClasse(Classe.operadorDivisao);
                        // System.out.println(token.toString());
                        return token;
                    case ':':
                        caractere = proximoChar();
                        if (caractere == '=') {
                            caractere = proximoChar();
                            token.setClasse(Classe.atribuicao);
                        } else {
                            token.setClasse(Classe.doisPontos);
                        }
                        // System.out.println(token.toString());
                        return token;
                    case ';':
                        caractere = proximoChar();
                        token.setClasse(Classe.pontoEVirgula);
                        // System.out.println(token.toString());
                        return token;
                    case ',':
                        caractere = proximoChar();
                        token.setClasse(Classe.virgula);
                        // System.out.println(token.toString());
                        return token;
                    case '.':
                        caractere = proximoChar();
                        token.setClasse(Classe.ponto);
                        // System.out.println(token.toString());
                        return token;
                    case '>':
                        caractere = proximoChar();
                        if (caractere == '=') {
                            caractere = proximoChar();
                            token.setClasse(Classe.operadorMaiorIgual);
                        } else {
                            token.setClasse(Classe.operadorMaior);
                        }
                        // System.out.println(token.toString());
                        return token;
                    case '<':
                        caractere = proximoChar();
                        if (caractere == '=') {
                            caractere = proximoChar();
                            token.setClasse(Classe.operadorMenorIgual);
                        } else if (caractere == '>') {
                            caractere = proximoChar();
                            token.setClasse(Classe.operadorDiferente);
                        } else {
                            token.setClasse(Classe.operadorMenor);
                        }
                        // System.out.println(token.toString());
                        return token;
                    case '=':
                        caractere = proximoChar();
                        token.setClasse(Classe.operadorIgual);
                        // System.out.println(token.toString());
                        return token;
                    case '(':
                        caractere = proximoChar();
                        token.setClasse(Classe.parentesesEsquerdo);
                        // System.out.println(token.toString());
                        return token;
                    case ')':
                        caractere = proximoChar();
                        token.setClasse(Classe.parentesesDireito);
                        // System.out.println(token.toString());
                        return token;
                    case '{':
                        caractere = proximoChar();
                        while (caractere != '}') {
                            if (caractere == '\n') {
                                linha++;
                                coluna = 0;
                            }
                            caractere = proximoChar();
                        }
                        caractere = proximoChar();
                        continue;
                    case '\'':
                        caractere = proximoChar();
                        while (caractere != '\'') {
                            if (caractere == '\n') {
                                token.setClasse(Classe.EOF);
                                // System.out.println(token.toString());
                                return token;
                            }
                            lexema.append(caractere);
                            caractere = proximoChar();
                        }
                        caractere = proximoChar();
                        token.setClasse(Classe.string);
                        token.setValor(new Valor(lexema.toString()));
                        // System.out.println(token.toString());
                        return token;
                    default:
                        System.out.println(caractere+", " + (int)caractere);
                        System.out.println("Erro no sistema");
                        caractere = proximoChar();
                        
                }
            }
        }
    }

    public TabelaSimbolos getTabelaSimbolos() {
        return tabelaSimbolos;
    }
}

