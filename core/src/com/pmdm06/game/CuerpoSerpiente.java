package com.pmdm06.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class CuerpoSerpiente {

    private int x, y;
    private Texture textura;

    public CuerpoSerpiente(Texture textura) {
        this.textura = textura;
    }

    //Establece la posición de la porción del cuerpo
    public void setPosicion(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //Añade una porción del cuerpo de la sepiente
    public void draw(Batch batch, int serpienteX, int serpienteY) {
        if (!(x == serpienteX && y == serpienteY))
            batch.draw(textura, x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
