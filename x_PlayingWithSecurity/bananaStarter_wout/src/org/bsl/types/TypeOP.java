package org.bsl.types;

import java.io.Serializable;

public enum TypeOP implements Serializable {

    NULL, REQ_RETRY, REQ_REGISTER, REP_REGISTER, REQ_PROJ, REP_PROJ, REQ_MAP_PROJ, REP_MAP_PROJ, REP_NAME_PROJ, REQ_NAME_PROJ, ADD_PROJECT, ACT_PROJECT, REQ_LOGIN, REP_LOGIN, REQ_ADD_EUROS, REP_ADD_EUROS, UNLOCK, LOCK, NOTIFEUROS, REP_WRONG_LOGIN

}
/*
 NULL - VAZIO
 REQ_REGISTER - ENVIA USER E PASS _ CLIENTE -> SERVER
 REP_REGISTER - ENVIA RESPOSTA _ SERVER -> CLIENTE
 REQ_PROJ - ENVIA UM PROJECTO PARA CRIAR _ CLIENTE -> SERVER
 REP_PROJ - SE CRIOU OU NÃO _ SERVER -> CLIENTE
 REQ_RETRY - ENVIA USERNAME APOS PERDA DE LIGACAO ESTANDO JA LOGADO _ CLIENTE -> SERVER
 ADD_PROJECT - ENVIA UM PROJECTO CRIADO PARA ADICIONAR À LISTA _ CLIENTE -> SERVER | SERVER -> CLIENTE
 ACT_PROJECT - ENVIA UM PROJECTO ALTERADO PARA MUDAR NA LISTA _ SERVER -> CLIENTE
 REQ_MAP_PROJ - PEDE A LISTA DE PROJECTOS INTEIRA _ CLIENTE -> SERVER
 REP_MAP_PROJ - RECEBE A LISTA DE PROJECTOS INTEIRA _ SERVER -> CLIENTE
 REQ_LOGIN - ENVIA DADOS DE LOGIN PARA VERIFICAR _ CLIENTE -> SERVER
 REP_LOGIN - ENVIA BOOLEAN SE O LOGIN ESTA CORRECTO _ SERVER -> CLIENTE
 REQ_ADD_EUROS - ENVIA DADOS SOBRE PROJECTO A SER FINANCIADO _ CLIENTE -> SERVER
 REP_ADD_EUROS - ENVIA SE CONSEGUIU ADICONAR _ SERVER -> CLIENTE
 UNLOCK - ENVIA PEDIDO PARA DESBLOQUEAR (ex:quando está bloqueado pelo proj activo) _ SERVER -> CLIENTE
 LOCK - ENVIA PEDIDO PARA BLOQUEAR ATE RECEBER UM UNLOCK _ SERVER -> CLIENTE
 REQ_NAME_PROJ - PERGUNTA SE JA EXISTE O NOME DO PROJECTO _ CLIENTE -> SERVER
 REP_NAME_PROJ - RESPOSTA COM 1 ou 0 NO INTEIRO1 _ SERVER -> CLIENTE
 NOTIFEUROS - NOTIFICA O CRIADOR QUE O SEU PROJECTO RECEBEU DINHEIRO _ SERVER -> CLIENTE
 REP_WRONG_LOGIN - NOTIFICA O UTILIZADOR QUE O LOGIN TEM PROBLEMAS
 */
