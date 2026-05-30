package com.erdv.dto;

public class GenerationCreneauxResponse {

    private int creneauxCrees;
    private int granulariteMinutes;
    private int joursGeneres;

    public GenerationCreneauxResponse(int creneauxCrees, int granulariteMinutes, int joursGeneres) {
        this.creneauxCrees = creneauxCrees;
        this.granulariteMinutes = granulariteMinutes;
        this.joursGeneres = joursGeneres;
    }

    public int getCreneauxCrees() {
        return creneauxCrees;
    }

    public void setCreneauxCrees(int creneauxCrees) {
        this.creneauxCrees = creneauxCrees;
    }

    public int getGranulariteMinutes() {
        return granulariteMinutes;
    }

    public void setGranulariteMinutes(int granulariteMinutes) {
        this.granulariteMinutes = granulariteMinutes;
    }

    public int getJoursGeneres() {
        return joursGeneres;
    }

    public void setJoursGeneres(int joursGeneres) {
        this.joursGeneres = joursGeneres;
    }
}
