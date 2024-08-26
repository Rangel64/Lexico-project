package sintatico;

import lexico.Classe;
import lexico.Lexico;
import lexico.Token;

public class Sintatico {
    private Lexico lexico;
    private String arquivoLeitura;
    private Token token;

    public Sintatico(String arquivo){
        this.arquivoLeitura = arquivo;
        lexico = new Lexico(arquivo);
    }

    public void analizar(){
        token = lexico.nextToken();
        programa();
    }

    // <programa> ::= program id {A01} ; <corpo> • {A45}
    public void programa(){
        if(token.getClasse() == Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("program")){
            token = lexico.nextToken();
            if(token.getClasse()==Classe.indentificador){
                token = lexico.nextToken();
                // {A01}
                if(token.getClasse()==Classe.pontoEVirgula){
                    token = lexico.nextToken();
                    corpo();
                    if(token.getClasse()==Classe.ponto){
                        token = lexico.nextToken();
                        //{A45}
                    }
                    else{
                        System.out.println(token.getLinha()+", "+token.getColuna() + " . " + " Erro sintatico: faltou ponto final no programa (.)");
                    }
                }
                else{
                    System.out.println(token.getLinha()+", "+token.getColuna() + " . " + " Erro sintatico: faltou ponto e virgula (;) depois do nome do programa");
                }
            }
            else{
                System.out.println(token.getLinha()+", "+token.getColuna() + " . " + " Erro sintatico: faltou o nome do programa");
            }
        }
        else{
            System.out.println(token.getLinha()+", "+token.getColuna() + " . " + " Erro sintatico: faltou comecar o programa com PROGRAM");
        }
        
    }

    // <corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}

    public void corpo(){
        declara();
        rotina();
        //{A44}
        if(token.getClasse()==Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("begin")){
            token = lexico.nextToken();
            sentencas();
            if(token.getClasse()==Classe.palavraReservada && token.getValor().getTexto().equalsIgnoreCase("end")){
                token = lexico.nextToken();
                
            }
            else{
                System.out.println(token.getLinha()+", "+token.getColuna() + " . " + " Erro sintatico: faltou terminar o corpo do programa com END");
            }
        }
        else{
            System.out.println(token.getLinha()+", "+token.getColuna() + " . " + " Erro sintatico: faltou comecar o corpo do programa com BEGIN");
        }
    }
    
}
