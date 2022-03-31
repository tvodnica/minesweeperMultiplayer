/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.model;

import java.io.Serializable;
import javafx.scene.control.Button;

/**
 *
 * @author Tomo
 */
public class MinefieldCell extends Button implements Serializable{

    private final int id;
    private boolean containsMine;
    private boolean clicked;
    private int surroundingMines;

    public int getSurroundingMines() {
        return surroundingMines;
    }

    public MinefieldCell(int id) {
        this.id = id;
        setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isContainsMine() {
        return containsMine;
    }

    public void setContainsMine(boolean containsMine) {
        this.containsMine = containsMine;
    }

    public void setSurroundingMines(int surroundingMines) {
        this.surroundingMines = surroundingMines;
    }
}
