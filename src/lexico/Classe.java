package lexico;

public enum Classe{
    indentificador,
    numeroInteiro,
    palavraReservada,
    operadorSoma, // +
    operadorSubtracao, // -
    operadorMultiplicacao, // *
    operadorDivisao, // /
    operadorMaior, // >
    operadorMenor, // <
    operadorMenorIgual, // <=
    operadorDiferente, // <>
    operadorMaiorIgual, // >=
    operadorIgual,  // =
    atribuicao,  // :=
    pontoEVirgula,  // ;
    virgula, // ,
    ponto,
    doisPontos, // :
    doisPontosIgual,//:=
    parentesesEsquerdo, // (
    parentesesDireito, // )
    string,
    EOF
 }