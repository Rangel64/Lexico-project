package sintatico;

import lexico.Categoria;
import lexico.Classe;
import lexico.Lexico;
import lexico.Token;

public class Sintatico{

    private Lexico lexico;
    private String aquivoLeitura;
    private Token token;



    public Sintatico(String arquivo){
        this.aquivoLeitura = arquivo;
        lexico = new Lexico(arquivo);
    }



    public void analisar(){
        token = lexico.nextToken();
        programa();
    }



    //<programa> ::= program id {A01} ; <corpo> • {A45}
    public void programa(){
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("program")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.identificador) {
                token = lexico.nextToken();
                // {A01}
                if (token.getClasse() == Classe.pontoEVirgula) {
                    token = lexico.nextToken();
                    corpo();
                    if (token.getClasse() == Classe.ponto) {
                        token = lexico.nextToken();
                        // {A45}
                    } else{
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto final no programa (.)");
                    }
                } else{
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula ( ; ) depois do nome do programa");
                }
            } else{
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou po nome do programa");
            }            
        }else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou começar o programa com PROGRAM");
        }
    }



    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}
    public void corpo(){
        declara();
        rotina();
        // {A44}
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("begin")) {
            token = lexico.nextToken();
            sentencas();
            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("end")){
                token = lexico.nextToken();
            }else{
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou terminar o corpo doprograma com END");
            }
        }else{
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou começar o corpo do programa com BEGIN");
        }
    }



    //<declara> ::= var<dvar> <mais_dc> | E
    public void declara() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("var")) {
            token = lexico.nextToken();
            dvar();
            mais_dc();
        }
    }



    //<dvar> ::= <variaveis> : <tipo_var> {A02}
    public void dvar() {
        variaveis();
        if (token.getClasse() == Classe.doisPontos) {
            token = lexico.nextToken();
            tipo_var();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou dois pontos (:) depois da declaração de variáveis");
        }
        // {A02}
    }



    //<variaveis> ::= id {A03} <mais_var>
    public void variaveis() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("id")) {
            token = lexico.nextToken();
            // {A03}
            mais_var();
        }
    }



    //<mais_var> ::=  ,  <variaveis> | ε
    public void mais_var() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();
            variaveis();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou vírgula (,) no final da atribuição do valor da variável");
        }
    }



    //<tipo_var> ::= integer
    public void tipo_var() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("integer")) {
            token = lexico.nextToken();
        }
    }



    //<mais_dc> ::= ; <cont_dc>
    public void mais_dc() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();
            cont_dc();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (;) no final de uma declaração de variáveis");
        }
    }



    //<cont_dc> ::= <dvar> <mais_dc> | ε
    public void cont_dc() {
        dvar();
        mais_dc();
    }



    //s<rotina> ::= <procedimento> | <funcao> | ε
    public void rotina() {
        procedimento();
        funcao();
    }



    //<procedimento> ::= procedure id {A04} <parametros> {A48}; <corpo> {A56} ; <rotina>
    public void procedimento() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("procedure")) {
            token = lexico.nextToken();

            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("id")) {
                token = lexico.nextToken();

                // {A04}

                parametros();

                // {A48}

                if (token.getClasse() == Classe.pontoEVirgula) {
                    token = lexico.nextToken();

                    corpo();

                    // {A56}

                    if (token.getClasse() == Classe.pontoEVirgula) {
                        token = lexico.nextToken();

                        rotina();
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (;) depois do corpo");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (;) depois dos parametros");
                }
            }
        }
    }



    //<parametros> ::= ( <lista_parametros> ) | ε
    public void parametros() {
        if (token.getClasse() == Classe.parentesesEsquerdo) {
            token = lexico.nextToken();

            lista_parametros();

            if (token.getClasse() == Classe.parentesesDireito) {
                token = lexico.nextToken();
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou parenteses direito ()) no fim da lista de parametros");
            }
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou parenteses esquedo (() no começo dos parametros");
        }
    }



    //<lista_parametros> ::= <lista_id> : <tipo_var> {A06} <cont_lista_par>
    public void lista_parametros() {
        lista_id();

        if (token.getClasse() == Classe.doisPontos) {
            token = lexico.nextToken();

            tipo_var();

            // {A06}

            cont_lista_par();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou dois pontos (:) depois do id na lista parametros");
        }
    }

    

    //<lista_id> ::= id {A07} <cont_lista_id>
    public void lista_id() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("id")) {
            token = lexico.nextToken();

            // {A07}

            cont_lista_id();
        }
    }



    //<cont_lista_id> ::= , <lista_id> | ε
    public void cont_lista_id() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();

            lista_id();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou vírgula (,) no começo do cont_lista_id");
        }
    }



    //<cont_lista_par> ::= ; <lista_parametros> | ε
    public void cont_lista_par() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();
            
            lista_parametros();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (;) no começo de cont lista par");
        }
    }



    //<tipo_funcao> ::= integer
    public void tipo_funcao() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("integer")) {
            token = lexico.nextToken();
        }
    }


    //<funcao> ::= function id {A05} <parametros> {A48} : <tipo_funcao> {A47} ; <corpo> {A56} ; <rotina>
    public void funcao() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("function")) {
            token = lexico.nextToken();

            if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("id")) {
                token = lexico.nextToken();

                // {A05}

                parametros();

                // {A48}

                if (token.getClasse() == Classe.doisPontos) {
                    token = lexico.nextToken();

                    tipo_funcao();

                    // {A47}

                    if (token.getClasse() == Classe.pontoEVirgula) {
                        token = lexico.nextToken();

                        corpo();

                        // {A56}

                        if (token.getClasse() == Classe.pontoEVirgula) {
                            token = lexico.nextToken();
                            
                            rotina();
                        } else {
                            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (;) depois do corpo");
                        }
                    } else {
                        System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (;) depois do tipo de função");
                    }
                } else {
                    System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou dois ponto (:) depois dos parametros");
                }
            }
        }
    }



    //<sentencas> ::= <comando> <mais_sentencas>
    public void sentencas() {
        comando();
        mais_sentencas();
    }

    //<comando> ::=
    //    read ( <var_read> ) |
    //    write ( <exp_write> ) |
    //    writeln ( <exp_write> ) {A61} |
    //    for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas> end {A13} |
    //    repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
    //    while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
    //    if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa> {A21} |
    //    id {A49} := <expressao> {A22} |
    //    <chamada_procedimento>
    private void comando() {
        if (token.getClasse() == Classe.palavraReservada &&
                token.getValor().getTexto().equals("read")) {
            token = lexico.nextToken();
            // {A04}
            if (token.getClasse() == Classe.parentesesEsquerdo) {
                token = lexico.nextToken();
                var_read();
                if (token.getClasse() == Classe.parentesesDireito) {
                    token = lexico.nextToken();
                    // {A05}
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
                    token.getValor().getTexto().equals("write")) {
                token = lexico.nextToken();
                // {A06}
                if (token.getClasse() == Classe.parentesesEsquerdo) {
                    token = lexico.nextToken();
                    exp_write();
                    if (token.getClasse() == Classe.parentesesDireito) {
                        token = lexico.nextToken();
                        // {A07}
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
                        token.getValor().getTexto().equals("writeln")) {
                    token = lexico.nextToken();
                    // {A06}
                    if (token.getClasse() == Classe.parentesesEsquerdo) {
                        token = lexico.nextToken();
                        exp_write();
                        if (token.getClasse() == Classe.parentesesDireito) {
                            // {61}
                            // WriteLn
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
                        token.getValor().getTexto().equals("for")) {
                    token = lexico.nextToken();
                    if (token.getClasse() == Classe.identificador) {
                        // {A57}
                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.atribuicao) {
                            token = lexico.nextToken();
                            expressao();
                            // {A11}
                            
                            if (token.getClasse() == Classe.palavraReservada &&
                                    token.getValor().getTexto().equals("to")) {
                                token = lexico.nextToken();
                                expressao();
                                // {A12}
                                
                                if (token.getClasse() == Classe.palavraReservada &&
                                        token.getValor().getTexto().equals("do")) {
                                    token = lexico.nextToken();
                                    if (token.getClasse() == Classe.palavraReservada &&
                                            token.getValor().getTexto().equals("begin")) {
                                        token = lexico.nextToken();
                                        sentencas();
                                        if (token.getClasse() == Classe.palavraReservada &&
                                                token.getValor().getTexto().equals("end")) {
                                            token = lexico.nextToken();
                                            // {A13}
                                            
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
                    } else {
                        System.err.println(token.getLinha() + ", " + token.getColuna() +
                                " - identificador esperado no for (comando).");
                    }
                    // repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
                } else if (token.getClasse() == Classe.palavraReservada &&
                        token.getValor().getTexto().equals("repeat")) {
                    //{A14}
                    sentencas();
                    if (token.getClasse() == Classe.palavraReservada &&
                            token.getValor().getTexto().equals("until")) {
                        token = lexico.nextToken();
                        if (token.getClasse() == Classe.parentesesEsquerdo) {
                            token = lexico.nextToken();
                            expressao_logica();
                            if (token.getClasse() == Classe.parentesesDireito) {
                                token = lexico.nextToken();
                                // A15
                                
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
                            && token.getValor().getTexto().equals("while")) {
                        token = lexico.nextToken();
                        // {A16}
                        

                        if (token.getClasse() == Classe.parentesesEsquerdo) {
                            token = lexico.nextToken();
                            expressao_logica();
                            if (token.getClasse() == Classe.parentesesDireito) {
                                token = lexico.nextToken();
                                // {A17}
                                
                                if (token.getClasse() == Classe.palavraReservada
                                        && token.getValor().getTexto().equals("do")) {
                                    token = lexico.nextToken();
                                    if (token.getClasse() == Classe.palavraReservada
                                            && token.getValor().getTexto().equals("begin")) {
                                        token = lexico.nextToken();
                                        sentencas();
                                        if (token.getClasse() == Classe.palavraReservada
                                                && token.getValor().getTexto().equals("end")) {
                                            token = lexico.nextToken();
                                            // {A18}
                                            
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
                                && token.getValor().getTexto().equals("if")) {
                            token = lexico.nextToken();
                            // {A11}
                            if (token.getClasse() == Classe.parentesesEsquerdo) {
                                token = lexico.nextToken();
                                expressao_logica();
                                if (token.getClasse() == Classe.parentesesDireito) {
                                    token = lexico.nextToken();
                                    // A{19}
                                    

                                    if (token.getClasse() == Classe.palavraReservada
                                            && token.getValor().getTexto().equals("then")) {
                                        token = lexico.nextToken();
                                        if (token.getClasse() == Classe.palavraReservada
                                                && token.getValor().getTexto().equals("begin")) {
                                            token = lexico.nextToken();
                                            sentencas();
                                            if (token.getClasse() == Classe.palavraReservada
                                                    && token.getValor().getTexto().equals("end")) {
                                                token = lexico.nextToken();
                                                // {A20}
                                                pfalsa();
                                                // {A21}
                                                
                                                if (token.getClasse() == Classe.palavraReservada
                                                        && token.getValor().getTexto().equals("else")) {
                                                    token = lexico.nextToken();
                                                    if (token.getClasse() == Classe.palavraReservada
                                                            && token.getValor().getTexto()
                                                                    .equals("begin")) {
                                                        token = lexico.nextToken();
                                                        sentencas();
                                                        if (token.getClasse() == Classe.palavraReservada
                                                                && token.getValor().getTexto()
                                                                        .equals("end")) {
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
                                
                                token = lexico.nextToken();
                                if (token.getClasse() == Classe.atribuicao) {
                                    token = lexico.nextToken();
                                    expressao();
                                    // {A22}
                                    
                                } else {
                                    System.err.println(token.getLinha() + "," + token.getColuna()
                                            + " Erro: era esperado um sinal de atribuição");
                                        }
                            }
                            else{
                                chamada_procedimento();
                            }
                        }
                    }
                }
            }
        }
    }

    // <chamada_procedimento> ::= <id_proc> {A50} <argumentos> {A23}
    public void chamada_procedimento(){
        if (token.getClasse() == Classe.identificador) {
            token = lexico.nextToken();
            // {A50}
            argumentos();
            // {A23}
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna()
                        + " Erro: era esperado um identificador");
        }
        
       
        
    }

    // <argumentos> ::= ( <lista_arg> ) | ε
    public void argumentos(){
        if (token.getClasse() == Classe.parentesesEsquerdo) {
            token = lexico.nextToken();
            lista_arg();
            if (token.getClasse() == Classe.parentesesDireito) {
                token = lexico.nextToken();
                
            } else {
                System.err.println(token.getLinha() + "," + token.getColuna()
                        + " Erro: era esperado um parênteses direito após a lista de variáveis");
            }
        }
    }

    // <lista_arg> ::= <expressao> <cont_lista_arg>
    void lista_arg() {
        expressao();
        cont_lista_arg();
    }

    void cont_lista_arg() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();

            lista_arg();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (,)");
        }
    }

    //<mais_sentencas> ::= ; <cont_sentencas>
    public void mais_sentencas() {
        if (token.getClasse() == Classe.pontoEVirgula) {
            token = lexico.nextToken();

            cont_sentencas();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou ponto e vírgula (;) no comeco de mais_sentencas");
        }
    }



    //<cont_sentencas> ::= <sentencas> | ε
    public void cont_sentencas() {
        sentencas();
    }



    //<var_read> ::= id {A08} <mais_var_read>
    public void var_read() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("id")) {
            token = lexico.nextToken();

            // {A08}

            mais_var_read();
        }
    }



    //<mais_var_read> ::= , <var_read> | ε
    public void mais_var_read() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();

            var_read();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou vírgula (,) no começo do mais_var_read");
        }
    }



    //<exp_write> ::= id {A09} <mais_exp_write> |
    public void exp_write() {
        if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("id")) {
            token = lexico.nextToken();

            // {A09}

            mais_exp_write();
        }

        else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("string")) {
            token = lexico.nextToken();

            // {A59}

            mais_exp_write();
        }
        
        else if (token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("intnum")) {
            token = lexico.nextToken();

            // {A43}

            mais_exp_write();
        }
    }



    //<mais_exp_write> ::=  ,  <exp_write> | ε
    public void mais_exp_write() {
        if (token.getClasse() == Classe.virgula) {
            token = lexico.nextToken();

            exp_write();
        } else {
            System.err.println(token.getLinha() + "," + token.getColuna() + " - " + "Erro sintático: faltou vírgula (,) no começo do mais_exp_write");
        }
    }

     // <pfalsa> ::= else {A25} begin <sentencas> end | ε
    private void pfalsa() {
        //{A25}
        if (token.getClasse() == Classe.palavraReservada
                && token.getValor().getTexto().equals("else")) {
            token = lexico.nextToken();
            if (token.getClasse() == Classe.palavraReservada
                    && token.getValor().getTexto().equals("begin")) {
                token = lexico.nextToken();
                sentencas();
                if (token.getClasse() == Classe.palavraReservada
                        && token.getValor().getTexto().equals("end")) {
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
                && token.getValor().getTexto().equals("or")) {
            token = lexico.nextToken();
            termo_logico();
            mais_expr_logica();
            // {A26}

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
                && token.getValor().getTexto().equals("and")) {
            token = lexico.nextToken();
            fator_logico();
            mais_termo_logico();
            // {A27}
            
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
        } else if (token.getValor().getTexto().equals("not")) {
            token = lexico.nextToken();
            fator_logico();
            // {A28}
            
        } else if (token.getValor().getTexto().equals("true")) {
            token = lexico.nextToken();
            // {A29}
            
        } else if (token.getValor().getTexto().equals("false")) {
            token = lexico.nextToken();
            // {A30}
            
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
            
        } else if (token.getClasse() == Classe.operadorMaior) {
            token = lexico.nextToken();
            expressao();
            // {A32}
            
        } else if (token.getClasse() == Classe.operadorMaiorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A33}
            
        } else if (token.getClasse() == Classe.operadorMenor) {
            token = lexico.nextToken();
            expressao();
            // {A34}
            
        } else if (token.getClasse() == Classe.operadorMenorIgual) {
            token = lexico.nextToken();
            expressao();
            // {A35}
            
        } else if (token.getClasse() == Classe.operadorDiferente) {
            token = lexico.nextToken();
            expressao();
        
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
            // {A37}
        } else if (token.getClasse() == Classe.operadorSubtracao) {
            token = lexico.nextToken();
            termo();
            // {A38}
            mais_expressao();
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
            mais_termo();
        } else if (token.getClasse() == Classe.operadorDivisao) {
            token = lexico.nextToken();
            fator();
            // {A40}
            mais_termo();
        }
    }

    // <fator> ::= <id> {A55} | <intnum> {A41} | ( <expressao> )
    private void fator() {
        if (token.getClasse() == Classe.identificador) {
            // {A55}
            
            token = lexico.nextToken();

        } else {
            if (token.getClasse() == Classe.numeroInteiro) {
                // {A41}
                
                token = lexico.nextToken();
            } else {
                if (token.getClasse() == Classe.parentesesEsquerdo) {
                    token = lexico.nextToken();
                    expressao();
                    if (token.getClasse() == Classe.parentesesDireito) {
                        token = lexico.nextToken();

                        if(token.getClasse() == Classe.identificador) {
                            // {A60}
                            argumentos();
                            // {A42}
                        }

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