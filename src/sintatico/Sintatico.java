package sintatico;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;    
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lexico.Categoria;
import lexico.Classe;
import lexico.Lexico;
import lexico.Registro;
import lexico.TabelaSimbolos;
import lexico.Tipo;
import lexico.Token;

public class Sintatico {
    private String nomeArquivo;
    private Lexico lexico;
    private Token token;

    private TabelaSimbolos tabela = new TabelaSimbolos();
    private String rotulo = "";
    private int contRotulo = 1;
    private int offSetVariavel = 0;
    private static final int TAMANHO_INTEIRO = 4;

    private String nomeArquivoSaida;
    private String caminhoArquivoSaida;
    private BufferedWriter bw;
    private FileWriter fw;

    private List<String> variaveis = new ArrayList<String>();
    private List<String> sectionData = new ArrayList<String>();

    private Registro registro;
    private String rotuloElse;

    public Sintatico(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
        lexico = new Lexico(nomeArquivo);

        nomeArquivoSaida = "queronemver.asm";
        caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();
        bw = null;
        fw = null;
        try {
            fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
            bw = new BufferedWriter(fw);
        } catch (Exception e) {
            System.err.println("Erro ao criar arquivo de saída");
        }

    }

    public void analisar() {
        System.out.println("Analisando " + nomeArquivo);
        token = lexico.nextToken();
        programa();
        try {
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String criarRotulo(String texto) {
        String retorno = "rotulo" + texto + contRotulo;
        contRotulo++;
        return retorno;
    }

    private void escreverCodigo(String instrucoes) {
        try {
            if (rotulo.isEmpty()) {
                bw.write(instrucoes + "\n");
            } else {
                bw.write(rotulo + ": " + instrucoes + "\n");
                rotulo = "";
            }
        } catch (IOException e) {
            System.err.println("Erro escrevendo no arquivo de saída");
        }
    }


    // <programa> ::= program <id> {A01} ; <corpo> • {A45}
    public void programa() {
        if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("program")) {
            token = lexico.nextToken();
            // Sempre que reconhecr um novo terminar receba o proximo token
            if (token.getClasse() == Classe.identificador) {
                
                // {A01}
                Registro registro = tabela.add(token.getValor().getTexto());
                offSetVariavel = 0;
                registro.setCategoria(Categoria.PROGRAMA_PRINCIPAL);
                escreverCodigo("global main");
                escreverCodigo("extern printf");
                escreverCodigo("extern scanf\n");
                escreverCodigo("section .text");
                rotulo = "main";
                escreverCodigo("\t; Entrada do programa");
                escreverCodigo("\tpush ebp");
                escreverCodigo("\tmov ebp, esp");
                System.out.println(tabela);

                token = lexico.nextToken();

                if (token.getClasse() == Classe.pontoEVirgula) {
                    token = lexico.nextToken();
                    corpo();
                    if (token.getClasse() == Classe.ponto) {
                        token = lexico.nextToken();
                        // {A45}

                        escreverCodigo("\tleave");
                        escreverCodigo("\tret");
                        if (!sectionData.isEmpty()) {
                            escreverCodigo("\nsection .data\n");
                            for (String mensagem : sectionData) {
                                escreverCodigo(mensagem);
                            }
                        }
                        try {
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            System.err.println("Erro ao fechar arquivo de saída");
                        }

                    } else {
                        System.err.println(token.getLinha() + ", " + token.getColuna() +
                                " - (.) Ponto final esperado no final do programa.");
                    }
                } else {
                    System.err.println(token.getLinha() + ", " + token.getColuna() +
                            " - (;) Ponto e vírgula esperado depois do nome do programa.");
                }
            } else {
                System.err.println(token.getLinha() + ", " + token.getColuna() +
                        " - Nome do programa principal esperado.");
            }

        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - Palavra reservada 'program' esperada no início do programa principal.");
        }
    }

    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    public void corpo() {
        declara();
        // rotina();
        // {A44}
        if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equalsIgnoreCase("begin")) {
            token = lexico.nextToken();
            sentencas();
            if (token.getClasse() == Classe.palavraReservada &&
                    token.getValor().getTexto().equalsIgnoreCase("end")) {
                token = lexico.nextToken();
                // {A46}
            } else {
                System.err.println(token.getLinha() + ", " + token.getColuna() +
                        " - Palavra reservada 'end' esperada no final do programa principal.");
            }
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - Palavra reservada 'begin' esperada no início do programa principal.");
        }
    }

    // <declara> ::= var <dvar> <mais_dc> | ε
    public void declara() {
        if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equalsIgnoreCase("var")) {
            token = lexico.nextToken();
            dvar();
            mais_dc();
        }
    }

    // <dvar> ::= <variaveis> : <tipo_var> {A02}
    public void dvar() {
        variaveis();
        if (token.getClasse() == Classe.doisPontos) {
            token = lexico.nextToken();
            tipo_var();
            int tamanho = 0;
            for (String var : variaveis) {
                tabela.getRegistro(var).setTipo(Tipo.INTEGER);
                tamanho += TAMANHO_INTEIRO;
            }
            escreverCodigo("\tsub esp, "+ tamanho);
            variaveis.clear();
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - (:) Dois pontos esperado depois das variaveis.");
        }
    }

    // <mais_dc> ::= ; <cont_dc>
    public void mais_dc() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();
            cont_dc();
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - (;) Ponto e vírgula esperado depois do mais_dc.");
        }
    }

    // <cont_dc> ::= <dvar> <mais_dc> | ε
    public void cont_dc() {
        if (token.getClasse() == Classe.identificador) {
            dvar();
            mais_dc();
        }
    }

    // <variaveis> ::= <id> {A03} <mais_var>
    public void variaveis() {
        if (token.getClasse() == Classe.identificador) {
            String variavel = token.getValor().getTexto();
            if (tabela.isPresent(variavel)) {
                System.err.println("Variável "+ variavel + "já foi declarada anteriormente");
                System.exit(-1);
            } else {
                tabela.add(variavel);
                tabela.getRegistro(variavel).setCategoria(Categoria.VARIAVEL);
                tabela.getRegistro(variavel).setOffset(offSetVariavel);
                offSetVariavel += TAMANHO_INTEIRO;
                variaveis.add(variavel);
            }
            token = lexico.nextToken();
            mais_var();
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - (identificador) Identificador esperado depois de variaveis.");
        }
    }

    // <tipo_var> ::= integer
    public void tipo_var() {
        if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equalsIgnoreCase("integer")) {
            token = lexico.nextToken();
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna()
                    + " - (tipo variavel) Esperado o tipo de variável Integer depois dos dois pontos.");
        }
    }

    // <mais_var> ::= , <variaveis> | ε
    public void mais_var() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            variaveis();
        }
    }

    // <sentencas> ::= <comando> <mais_sentencas>
    public void sentencas() {
        comando();
        mais_sentencas();
    }

    /*
     * if( (token.getClasse() == Classe.palavraReservada && () )
     * || (token.getClasse()) ){
     */
    public void mais_sentencas() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();
            cont_sentencas();
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - (;) Ponto e vírgula esperado depois de mais_sentencas.");
        }
    }

    // <cont_sentencas> ::= <sentencas> | ε
    public void cont_sentencas() {
        if ((token.getClasse() == Classe.palavraReservada
                && ((token.getValor().getTexto().equalsIgnoreCase("read"))
                        || (token.getValor().getTexto().equalsIgnoreCase("write"))
                        || (token.getValor().getTexto().equalsIgnoreCase("writeln"))
                        || (token.getValor().getTexto().equalsIgnoreCase("for"))
                        || (token.getValor().getTexto().equalsIgnoreCase("repeat"))
                        || (token.getValor().getTexto().equalsIgnoreCase("while"))
                        || (token.getValor().getTexto().equalsIgnoreCase("if"))))
                || (token.getClasse() == Classe.identificador)) {
            sentencas();
        }
    }

    // <var_read> ::= <id> {A08} <mais_var_read>
    public void var_read() {
        if (token.getClasse() == Classe.identificador) {
            // {A08}

            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável " + variavel + " não foi declarada");
                System.exit(-1);
            } else {
                Registro registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("Identificador " + variavel + " não é uma variável");
                    System.exit(-1);
                } else {
                    escreverCodigo("\tmov edx, ebp");
                    escreverCodigo("\tlea eax, [edx - " + registro.getOffset() + "]");
                    escreverCodigo("\tpush eax");
                    escreverCodigo("\tpush @Integer");
                    escreverCodigo("\tcall scanf");
                    escreverCodigo("\tadd esp, 8");
                    if (!sectionData.contains("@Integer: db '%d',0")) {
                        sectionData.add("@Integer: db '%d',0");
                    }
                }
            }

            token = lexico.nextToken();
            mais_var_read();
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - (identificador) Identificador esperado depois da função var_read().");
        }
    }

    // <mais_var_read> ::= , <var_read> | ε
    public void mais_var_read() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            var_read();
        }
    }

    /*
     * <exp_write> ::= <id> {A09} <mais_exp_write> |
     * <string> {A59} <mais_exp_write> |
     * <intnum> {A43} <mais_exp_write>
     */
    public void exp_write() {
        if ((token.getClasse() == Classe.identificador)
                || (token.getClasse() == Classe.string)
                || (token.getClasse() == Classe.numeroInteiro)) {

            // <id> {A09} <mais_exp_write>
            if (token.getClasse() == Classe.identificador) {
                // {A09}

                String variavel = token.getValor().getTexto();
                if (!tabela.isPresent(variavel)) {
                    System.err.println("Variável "+ variavel + "não foi declarada");
                    System.exit(-1);
                } else {
                    Registro registro = tabela.getRegistro(variavel);
                    if (registro.getCategoria() != Categoria.VARIAVEL) {
                        System.err.println("Identificador "+ variavel + "não é uma variável");
                        System.exit(-1);
                    } else {
                        escreverCodigo("\tpush dword[ebp - "+ registro.getOffset() + "]");
                        escreverCodigo("\tpush @Integer");
                        escreverCodigo("\tcall printf");
                        escreverCodigo("\tadd esp, 8");
                        if (!sectionData.contains("@Integer: db '%d',0")) {
                            sectionData.add("@Integer: db '%d',0");
                        }
                    }
                }

                token = lexico.nextToken();
                mais_exp_write();
            } else if (token.getClasse() == Classe.string) {
                // {A59}
                String string = token.getValor().getTexto();
                String rotulo = criarRotulo("String");
                sectionData.add(rotulo + ": db '"+ string + "',0");
                escreverCodigo("\tpush "+ rotulo);
                escreverCodigo("\tcall printf");
                escreverCodigo("\tadd esp, 4");
                token = lexico.nextToken();
                mais_exp_write();
            } else if (token.getClasse() == Classe.numeroInteiro) {
                // {A43}
                token = lexico.nextToken();
                mais_exp_write();
            }
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - Identificador/string,/numeroInteiro esperado ao ler a função exp_write()");
        }
    }

    // <mais_exp_write> ::= , <exp_write> | ε
    public void mais_exp_write() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            exp_write();
        }
    }

    private void comando() {
        if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equalsIgnoreCase("read")) {
            token = lexico.nextToken();
            
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                var_read();
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna()
                            + " Erro: era esperado um parênteses direito após a lista de variáveis");
                }
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna()
                        + " Erro: era esperado um sinal de atribuição após o identificador");
            }
        } else {
            if (token.getClasse() == Classe.palavraReservada &&
                    token.getValor().getTexto().equalsIgnoreCase("write")) {
                token = lexico.nextToken();
                
                if (token.getClasse() == Classe.parentesesEsquerdo) {
                    token = lexico.nextToken();
                    exp_write();
                    if (token.getClasse() == Classe.parentesesDireito) {
                        token = lexico.nextToken();
                        
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " Erro: era esperado um parênteses direito após a lista de expressões");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna()
                            + " Erro: era esperado um sinal de atribuição após o identificador");
                }
            } else {
                if (token.getClasse() == Classe.palavraReservada &&
                        token.getValor().getTexto().equalsIgnoreCase("writeln")) {
                    token = lexico.nextToken();
                    
                    if (token.getClasse() == Classe.parentesesEsquerdo) {
                        token = lexico.nextToken();
                        exp_write();
                        if (token.getClasse() == Classe.parentesesDireito) {
                            // {A61}
                            
                            String novaLinha = "rotuloStringLN: db '',10,0";
                            if (!sectionData.contains(novaLinha)) {
                                sectionData.add(novaLinha);
                            }
                            escreverCodigo("\tpush rotuloStringLN");
                            escreverCodigo("\tcall printf");
                            escreverCodigo("\tadd esp, 4");

                            token = lexico.nextToken();
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna()
                                    + " Erro: era esperado um parênteses direito após a lista de expressões");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " Erro: era esperado um sinal de atribuição após o identificador");
                    }
                } else if (token.getClasse() == Classe.palavraReservada &&
                        token.getValor().getTexto().equalsIgnoreCase("for")) {
                    token = lexico.nextToken();
                    if (token.getClasse() == Classe.identificador) {
                        // {A57}

                        String variavel = token.getValor().getTexto();
                        if (!tabela.isPresent(variavel)) {
                            System.err.println("Variável "+ variavel + "não foi declarada");
                            System.exit(-1);
                        } else {
                            registro = tabela.getRegistro(variavel);
                            if (registro.getCategoria() != Categoria.VARIAVEL) {
                                System.err.println("O identificador "+ variavel + "não é uma variável. A57");
                                System.exit(-1);
                            }
                        }

                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.atribuicao) {
                            token = lexico.nextToken();
                            expressao();
                            // {A11}

                            escreverCodigo("\tpop dword[ebp - "+ registro.getOffset() + "]");
                            String rotuloEntrada = criarRotulo("FOR");
                            String rotuloSaida = criarRotulo("FIMFOR");
                            rotulo = rotuloEntrada;

                            if (token.getClasse() == Classe.palavraReservada &&
                                    token.getValor().getTexto().equalsIgnoreCase("to")) {
                                token = lexico.nextToken();
                                expressao();
                                // {A12}
                                escreverCodigo("\tpush ecx\n"
                                    + "\tmov ecx, dword[ebp - "+ registro.getOffset() + "]\n"
                                    + "\tcmp ecx, dword[esp+4]\n"// +4 por causa do ecx
                                    + "\tjg "+ rotuloSaida + "\n"
                                    + "\tpop ecx");
                                if (token.getClasse() == Classe.palavraReservada &&
                                        token.getValor().getTexto().equalsIgnoreCase("do")) {
                                    token = lexico.nextToken();
                                    if (token.getClasse() == Classe.palavraReservada &&
                                            token.getValor().getTexto().equalsIgnoreCase("begin")) {
                                        token = lexico.nextToken();
                                        sentencas();
                                        if (token.getClasse() == Classe.palavraReservada &&
                                                token.getValor().getTexto().equalsIgnoreCase("end")) {
                                            token = lexico.nextToken();
                                            // {A13}
                                            escreverCodigo("\tadd dword[ebp - "+ registro.getOffset() + "], 1");
                                            escreverCodigo("\tjmp "+ rotuloEntrada);
                                            rotulo = rotuloSaida;
                                        } else {
                                            System.err.println(token.getLinha() + ", " + token.getColuna() +
                                                    " - end esperado no for (comando).");
                                        }
                                    } else {
                                        System.err.println(token.getLinha() + ", " + token.getColuna() +
                                                " - begin esperado no for (comando).");
                                    }
                                } else {
                                    System.err.println(token.getLinha() + ", " + token.getColuna() +
                                            " - do esperado no for (comando).");
                                }
                            } else {
                                System.err.println(token.getLinha() + ", " + token.getColuna() +
                                        " - to esperado no for (comando).");
                            }
                        } else {
                            System.err.println(token.getLinha() + ", " + token.getColuna() +
                                    " - (:=) atribuição esperada no for (comando).");
                        }
                        
                    }   
                    // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
                } else if (token.getClasse() == Classe.palavraReservada &&
                    token.getValor().getTexto().equalsIgnoreCase("repeat")) {
                    String rotRepeat = criarRotulo("repeat");
                    rotulo = rotRepeat;
                    token = lexico.nextToken();
                    sentencas();
                    if (token.getClasse() == Classe.palavraReservada &&
                            token.getValor().getTexto().equalsIgnoreCase("until")) {
                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.parentesesEsquerdo) {
                            token = lexico.nextToken();
                            expressao_logica();
                            if (token.getClasse() == Classe.parentesesDireito) {
                                token = lexico.nextToken();
                                // A15

                                escreverCodigo("\tcmp dword[esp], 0");
                                escreverCodigo("\tje "+ rotRepeat);

                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna()
                                        + " Erro: era esperado um parênteses direito");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna()
                                    + " Erro: era esperado um parênteses esquerdo");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " Erro: era esperada a palavra reservada until");
                    }
                } else {
                    if (token.getClasse() == Classe.palavraReservada
                            && token.getValor().getTexto().equalsIgnoreCase("while")) {
                        token = lexico.nextToken();
                        // {A16}
                        String rotuloWhile = criarRotulo("While");
                        String rotuloFim = criarRotulo("FimWhile");
                        rotulo = rotuloWhile;
                        if (token.getClasse() == Classe.parentesesEsquerdo) {
                            token = lexico.nextToken();
                            expressao_logica(); 
                            if (token.getClasse() == Classe.parentesesDireito) {
                                token = lexico.nextToken();
                                // {A17}
                                escreverCodigo("\tcmp dword[esp], 0");
                                escreverCodigo("\tje "+ rotuloFim);
                                if (token.getClasse() == Classe.palavraReservada
                                        && token.getValor().getTexto().equalsIgnoreCase("do")) {
                                    token = lexico.nextToken();
                                    if (token.getClasse() == Classe.palavraReservada
                                            && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                                        token = lexico.nextToken();
                                        sentencas();
                                        if (token.getClasse() == Classe.palavraReservada
                                                && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                            token = lexico.nextToken();
                                            // {A18}
                                            escreverCodigo("\tjmp "+ rotuloWhile);
                                            rotulo = rotuloFim;
                                        } else {
                                            System.err.println(token.getLinha() + "," + token.getColuna()
                                                    + " Erro: era esperada a palavra reservada end");
                                        }
                                    } else {
                                        System.err.println(token.getLinha() + "," + token.getColuna()
                                                + " Erro: era esperada a palavra reservada begin");
                                    }
                                } else {
                                    System.err.println(token.getLinha() + "," + token.getColuna()
                                            + " Erro: era esperada a palavra reservada do");
                                }
                            } else {
                                System.err.println(token.getLinha() + "," + token.getColuna()
                                        + " Erro: era esperado um parênteses direito");
                            }
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna()
                                    + " Erro: era esperado um parênteses esquerdo");
                        }
                    } else {
                        if (token.getClasse() == Classe.palavraReservada
                                && token.getValor().getTexto().equalsIgnoreCase("if")) {
                            token = lexico.nextToken();
                            // {A11}
                            if (token.getClasse() == Classe.parentesesEsquerdo) {
                                token = lexico.nextToken();
                                expressao_logica();
                                if (token.getClasse() == Classe.parentesesDireito) {
                                    token = lexico.nextToken();
                                    // {A19}
                                    rotuloElse = criarRotulo("Else");
                                    String rotuloFim = criarRotulo("FimIf");
                                    escreverCodigo("\tcmp dword[esp], 0\n");
                                    escreverCodigo("\tje "+ rotuloElse);
                                    if (token.getClasse() == Classe.palavraReservada
                                            && token.getValor().getTexto().equalsIgnoreCase("then")) {
                                        token = lexico.nextToken();
                                        if (token.getClasse() == Classe.palavraReservada
                                                && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                                            token = lexico.nextToken();
                                            sentencas();
                                            if (token.getClasse() == Classe.palavraReservada
                                                    && token.getValor().getTexto().equalsIgnoreCase("end")) {
                                                token = lexico.nextToken();
                                                // {A20}
                                                escreverCodigo("\tjmp "+ rotuloFim);    
                                                // {A21}
                                                rotulo = rotuloFim;
                                                if (token.getClasse() == Classe.palavraReservada
                                                        && token.getValor().getTexto().equalsIgnoreCase("else")) {
                                                    token = lexico.nextToken();
                                                    if (token.getClasse() == Classe.palavraReservada
                                                            && token.getValor().getTexto()
                                                                    .equalsIgnoreCase("begin")) {
                                                        token = lexico.nextToken();
                                                        sentencas();
                                                        if (token.getClasse() == Classe.palavraReservada
                                                                && token.getValor().getTexto()
                                                                        .equalsIgnoreCase("end")) {
                                                            token = lexico.nextToken();
                                                        } else {
                                                            System.err.println(token.getLinha() + ","
                                                                    + token.getColuna()
                                                                    + " Erro: era esperada a palavra reservada end");
                                                        }
                                                    } else {
                                                        System.err.println(token.getLinha() + ","
                                                                + token.getColuna()
                                                                + " Erro: era esperada a palavra reservada begin ");
                                                    }
                                                }
                                                // else {
                                                // System.err.println(token.getLinha() + ","
                                                // + token.getColuna()
                                                // + " Erro: era a Palavra Reserva Else após um If");
                                                // }
                                            } else {
                                                System.err.println(token.getLinha() + "," + token.getColuna()
                                                        + " Erro: era a Palavra Reserva end");
                                            }
                                        } else {
                                            System.err.println(token.getLinha() + "," + token.getColuna()
                                                    + " Erro: era esperada a palavra reservada begin");
                                        }
                                    } else {
                                        System.err.println(token.getLinha() + "," + token.getColuna()
                                                + " Erro: era esperada a palavra reservada then");
                                    }
                                } else {
                                    System.err.println(token.getLinha() + "," + token.getColuna());
                                }
                            }
                        } else {
                            if (token.getClasse() == Classe.identificador) {
                                // {A49}
                                // Verificar se o identificador id está na tabela de símbolos corrente ou nas
                                // apontadas por tabelaPai. Caso não esteja, emitir mensagem apropriada dizendo
                                // que o mesmo ainda não foi declarado.
                                // Caso contrário, verificar se sua categoria é variável, parâmetro ou é a
                                // função corrente. Caso não seja, emitir mensagem indicando que o identificador
                                // não é uma variável.
                                
                                String variavel = token.getValor().getTexto();
                                if (!tabela.isPresent(variavel)) {
                                    System.err.println("Variável "+ variavel + "não foi declarada");
                                    System.exit(-1);
                                } else {
                                    registro = tabela.getRegistro(variavel);
                                    if (registro.getCategoria() != Categoria.VARIAVEL) {
                                        System.err.println("O identificador "+ variavel + "não é uma variável. A49");
                                        System.exit(-1);
                                    }
                                }

                                token = lexico.nextToken();
                                if (token.getClasse() == Classe.atribuicao) {
                                    token = lexico.nextToken();
                                    expressao();
                                    // {A22}
                                    registro = tabela.getRegistro(variavel);
                                    escreverCodigo("\tpop eax");
                                    escreverCodigo("\tmov dword[ebp - "+ registro.getOffset() + "], eax");
                                    
                                } else {
                                    System.err.println(token.getLinha() + "," + token.getColuna()
                                            + " Erro: era esperado um sinal de atribuição");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // <pfalsa> ::= else {A25} begin <sentencas> end | ε
    private void pfalsa() {
        if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("else")) {
            token = lexico.nextToken();
            // {A25}
            escreverCodigo(rotuloElse + ":");
            if (token.getClasse() == Classe.palavraReservada
                    && token.getValor().getTexto().equalsIgnoreCase("begin")) {
                token = lexico.nextToken();
                sentencas();
                if (token.getClasse() == Classe.palavraReservada
                        && token.getValor().getTexto().equalsIgnoreCase("end")) {
                    token = lexico.nextToken();
                }
            }
        }
    }

    // <expressao_logica> ::= <termo_logico> <mais_expr_logica>
    private void expressao_logica() {
        termo_logico();
        mais_expr_logica();
    }

    // <mais_expr_logica> ::= or <termo_logico> <mais_expr_logica> {A26} | ε
    private void mais_expr_logica() {
        if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("or")) {
            token = lexico.nextToken();
            termo_logico();
            mais_expr_logica();

            String rotSaida = criarRotulo("SaidaMEL");
            String rotVerdade = criarRotulo("VerdadeMEL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tje "+ rotVerdade);
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tje "+ rotVerdade);    
            escreverCodigo("\tmov dword [ESP + 4], 0");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotVerdade;
            escreverCodigo("\tmov dword [ESP + 4], 1");
            rotulo = rotSaida;
            escreverCodigo("\tadd esp, 4");
        }
    }

    // <termo_logico> ::= <fator_logico> <mais_termo_logico>
    private void termo_logico() {
        fator_logico();
        mais_termo_logico();
    }

    // <mais_termo_logico> ::= and <fator_logico> <mais_termo_logico> {A27} | ε
    private void mais_termo_logico() {
        if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equalsIgnoreCase("and")) {
            token = lexico.nextToken();
            fator_logico();
            mais_termo_logico();
            // {A27}
            String rotSaida = criarRotulo("SaidaMTL");
            String rotFalso = criarRotulo("FalsoMTL");
            escreverCodigo("\tcmp dword [ESP + 4], 1");
            escreverCodigo("\tjne "+ rotFalso);
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        }
    }

    // <fator_logico> ::= <relacional> |
    // ( <expressao_logica> ) |
    // not <fator_logico> {A28} |
    // true {A29} |
    // false {A30}
    private void fator_logico() {
        if (token.getClasse() == Classe.parentesesEsquerdo) {
            token = lexico.nextToken();
            expressao_logica();
            if (token.getClasse() == Classe.parentesesDireito) {
                token = lexico.nextToken();
            } else {
                System.err.println(token.getLinha() + ", " + token.getColuna() +
                        " - ()) parênteses direito esperado (fator_logico).");
            }
        } else if (token.getValor().getTexto().equalsIgnoreCase("not")) {
            token = lexico.nextToken();
            fator_logico();
            // {A28}
            // Empilhar 1 (verdadeiro), caso o valor de fator_logico seja 0, e 0 (falso),
            // caso seja diferente. Proceda da seguinte forma:
            // Crie um rótulo Falso e outro Saida.

            String rotFalso = criarRotulo("FalsoFL");
            String rotSaida = criarRotulo("SaidaFL");
            escreverCodigo("\tcmp dword [ESP], 1");
            escreverCodigo("\tjne "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 0");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 1");
            rotulo = rotSaida;

        } else if (token.getValor().getTexto().equalsIgnoreCase("true")) {
            token = lexico.nextToken();
            // {A29}
            escreverCodigo("\tpush 1");
        } else if (token.getValor().getTexto().equalsIgnoreCase("false")) {
            token = lexico.nextToken();
            // {A30}
            escreverCodigo("\tpush 0");
        } else {
            relacional();
        }
    }

    // <relacional> ::= <expressao> = <expressao> {A31} |
    // <expressao> > <expressao> {A32} |
    // <expressao> >= <expressao> {A33} |
    // <expressao> < <expressao> {A34} |
    // <expressao> <= <expressao> {A35} |
    // <expressao> <> <expressao> {A36}
    private void relacional() {
        expressao();
        if (token.getClasse() == Classe.operadorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A31}

            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjne "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;

        } else if (token.getClasse() == Classe.operadorMaior) {
            token = lexico.nextToken();
            expressao();
            // {A32}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjle "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorMaiorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A33}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjl "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorMenor) {
            token = lexico.nextToken();
            expressao();
            // {A34}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjge "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorMenorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A35}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tjg "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else if (token.getClasse() == Classe.operadorDiferente) {
            token = lexico.nextToken();
            expressao();
            // {A36}
            String rotFalso = criarRotulo("FalsoREL");
            String rotSaida = criarRotulo("SaidaREL");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tcmp dword [ESP], eax");
            escreverCodigo("\tje "+ rotFalso);
            escreverCodigo("\tmov dword [ESP], 1");
            escreverCodigo("\tjmp "+ rotSaida);
            rotulo = rotFalso;
            escreverCodigo("\tmov dword [ESP], 0");
            rotulo = rotSaida;
        } else {
            System.err.println(token.getLinha() + ", " + token.getColuna() +
                    " - Operador relacional (=, <, <=, >, >= <>) esperado (relacional).");
        }
    }

    // <expressao> ::= <termo> <mais_expressao>
    private void expressao() {
        termo();
        mais_expressao();
    }

    // <mais_expressao> ::= + <termo> <mais_expressao> {A37} |
    // - <termo> <mais_expressao> {A38} | ε
    private void mais_expressao() {
        if (token.getClasse() == Classe.operadorSoma) {
            token = lexico.nextToken();
            termo();
            mais_expressao();
            // {A37}
            escreverCodigo("\tpop eax");
            escreverCodigo("\tadd dword[ESP], eax");
        } else if (token.getClasse() == Classe.operadorSubtracao) {
            token = lexico.nextToken();
            termo();
            mais_expressao();
            // {A38}
            escreverCodigo("\tpop eax");
            escreverCodigo("\tsub dword[ESP], eax");
        }
    }

    // <termo> ::= <fator> <mais_termo>
    private void termo() {
        fator();
        mais_termo();
    }

    // <mais_termo> ::= * <fator> <mais_termo> {A39} | / <fator> <mais_termo> {A40}
    // | ε
    private void mais_termo() {
        if (token.getClasse() == Classe.operadorMultiplicacao) {
            token = lexico.nextToken();
            fator();
            // {A39}
            escreverCodigo("\tpop eax");
            escreverCodigo("\timul eax, dword [ESP]");
            escreverCodigo("\tmov dword [ESP], eax");
            mais_termo();
            
        } else if (token.getClasse() == Classe.operadorDivisao) {
            token = lexico.nextToken();
            fator();
            // {A40}
            escreverCodigo("\tpop ecx");
            escreverCodigo("\tpop eax");
            escreverCodigo("\tidiv ecx");
            escreverCodigo("\tpush eax");
            mais_termo();
            
        }
    }

    // <fator> ::= <id> {A55} | <intnum> {A41} | ( <expressao> )
    private void fator() {
        if (token.getClasse() == Classe.identificador) {
            // {A55} 

            String variavel = token.getValor().getTexto();
            if (!tabela.isPresent(variavel)) {
                System.err.println("Variável "+ variavel + "não foi declarada");
                System.exit(-1);
            } else {
                registro = tabela.getRegistro(variavel);
                if (registro.getCategoria() != Categoria.VARIAVEL) {
                    System.err.println("O identificador "+ variavel + "não é uma variável. A55");
                    System.exit(-1);
                }
            }
            escreverCodigo("\tpush dword[ebp - "+ registro.getOffset() + "]");

            token = lexico.nextToken();

        } else {
            if (token.getClasse() == Classe.numeroInteiro) {
                // {A41}
                escreverCodigo("\tpush "+ token.getValor().getInteiro());
                token = lexico.nextToken();
            } else {
                if (token.getClasse() == Classe.parentesesEsquerdo) {
                    token = lexico.nextToken();
                    expressao();
                    if (token.getClasse() == Classe.parentesesDireito) {
                        token = lexico.nextToken();
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna()
                                + " Erro: era esperado um parênteses direito");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna()
                            + " Erro: era esperado um identificador, número inteiro ou parênteses esquerdo");
                }
            }
        }
    }
}