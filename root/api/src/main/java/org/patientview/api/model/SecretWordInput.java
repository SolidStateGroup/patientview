package org.patientview.api.model;

/**
 * SecretWordInput, used when updating a Users secret word (used in authentication)
 * Created by jamesr@solidstategroup.com
 * Created on 15/02/2016
 */
public class SecretWordInput {

    private String oldSecretWord;
    private String secretWord1;
    private String secretWord2;

    public SecretWordInput() { }

    public SecretWordInput(String secretWord1, String secretWord2) {
        this.secretWord1 = secretWord1;
        this.secretWord2 = secretWord2;
    }

    public SecretWordInput(String oldSecretWord, String secretWord1, String secretWord2) {
        this.oldSecretWord = oldSecretWord;
        this.secretWord1 = secretWord1;
        this.secretWord2 = secretWord2;
    }

    public String getOldSecretWord() {
        return oldSecretWord;
    }

    public void setOldSecretWord(String oldSecretWord) {
        this.oldSecretWord = oldSecretWord;
    }

    public String getSecretWord1() {
        return secretWord1;
    }

    public void setSecretWord1(String secretWord1) {
        this.secretWord1 = secretWord1;
    }

    public String getSecretWord2() {
        return secretWord2;
    }

    public void setSecretWord2(String secretWord2) {
        this.secretWord2 = secretWord2;
    }
}
