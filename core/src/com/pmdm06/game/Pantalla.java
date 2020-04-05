package com.pmdm06.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Pantalla extends ScreenAdapter {

    private SpriteBatch batch;
    private Texture cabezaSerpiente;
    private Texture manzana;
    private Texture cuerpoSerpiente;

    //Temporizadores
    private static final float TURNO = 0.2F;    //200 milisegundos
    private float timer = TURNO;

    //Por cada movimiento se añaden 32 pixeles a la posición de la imagen
    private static final int CELDA = 32;

    //Mostrar manzana
    private boolean existeManzana = false;

    //Cuerpo de la serpiente
    private Array<CuerpoSerpiente> cuerpo = new Array<CuerpoSerpiente>();

    //La serpiente no puede girar en setido contrario
    private boolean direccionFijada =false;

    //La sepiente choca consigo misma
    private boolean haChocado = false;

    //Estado del juego
    private enum ESTADO {INICIADO, FINALIZADO}
    private ESTADO estado = ESTADO.INICIADO;

    //Textos
    private BitmapFont bitmapFont;
    private GlyphLayout layout = new GlyphLayout();
    private static final String MENSAJE_FIN = "¡GAME OVER! Pulsa < ESPACIO > para jugar.";

    //Posiciones
    private int serpienteX = 0, serpienteY = 0;
    private int serpienteX_old = 0, serpienteY_old = 0;
    private int manzanaX, manzanaY;

    //Dirección
    private static final int DERECHA = 0;
    private static final int IZQUIERDA = 1;
    private static final int ARRIBA = 2;
    private static final int ABAJO = 3;
    private int direccion = DERECHA;


    //Este método se llama cuando la pantalla se convierte en la pantalla actual del juego.
    @Override
    public void show() {
        batch = new SpriteBatch();
        cabezaSerpiente = new Texture(Gdx.files.internal("cabeza.png"));
        manzana = new Texture(Gdx.files.internal("manzana.png"));
        cuerpoSerpiente = new Texture(Gdx.files.internal("cuerpo.png"));
        bitmapFont = new BitmapFont();
    }

    //El método render() se llama en cada ciclo. Por defecto, 60 veces por segundo.
    @Override
    public void render(float delta) {
        switch(estado) {
            case INICIADO:
                capturarTeclasCursor();
                actualizarSerpiente(delta);
                comerManzana();
                generarManzana();
            break;
            case FINALIZADO:
                capturarTeclaEspacio();
            break;
        }
        borrarPantalla();
        draw();
    }

    // Si la serpiente desaparece de la ventana se termina el juego
    private void comprobarColisionBordePantalla() {
        if (serpienteX >= Gdx.graphics.getWidth())  estado = ESTADO.FINALIZADO;
        if (serpienteX < 0)                         estado = ESTADO.FINALIZADO;
        if (serpienteY >= Gdx.graphics.getHeight()) estado = ESTADO.FINALIZADO;
        if (serpienteY < 0)                         estado = ESTADO.FINALIZADO;
    }

    //Mueve la serpiente
    private void moverSerpiente() {

        //Hay que tener en cuenta el tamaño del cuerpo de la serpiente
        serpienteX_old = serpienteX;
        serpienteY_old = serpienteY;

        switch (direccion) {
            case DERECHA:   serpienteX += CELDA; return;
            case IZQUIERDA: serpienteX -= CELDA; return;
            case ARRIBA:    serpienteY += CELDA; return;
            case ABAJO:     serpienteY -= CELDA; return;
        }
    }

    //Detecta las pulsaciones del teclado y cambia la dirección
    private void capturarTeclasCursor() {
        boolean pulsoIzq = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean pulsoDer = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean pulsoArr = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean pulsoAba = Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if (pulsoIzq) cambiarDireccion(IZQUIERDA);
        if (pulsoDer) cambiarDireccion(DERECHA);
        if (pulsoArr) cambiarDireccion(ARRIBA);
        if (pulsoAba) cambiarDireccion(ABAJO);
    }

    //Calcula una posición aleatoria para la manzana
    private void generarManzana() {
        if (!existeManzana) {
            do {
                manzanaX = MathUtils.random(Gdx.graphics.getWidth() / CELDA - 1) * CELDA;
                manzanaY = MathUtils.random(Gdx.graphics.getHeight() / CELDA - 1) * CELDA;
                existeManzana = true;
            } while (manzanaX == serpienteX && manzanaY == serpienteY);
        }
    }

    //Borra la pantalla
    private void borrarPantalla() {
        //Fondo negro
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    //Añade las texturas
    private void draw() {
        batch.begin();

        //Añade la cabeza
        batch.draw(cabezaSerpiente, serpienteX, serpienteY);

        //Añade el cuerpo
        for (CuerpoSerpiente cuerpoSerpiente : cuerpo)
            cuerpoSerpiente.draw(batch, serpienteX, serpienteY);

        //Añade la manzana
        if (existeManzana)
            batch.draw(manzana, manzanaX, manzanaY);

        //Textos
        if (estado == ESTADO.FINALIZADO) {
            layout.setText(bitmapFont, MENSAJE_FIN);
            bitmapFont.setColor(255,0,0,1);
            bitmapFont.draw(batch, MENSAJE_FIN, (Gdx.graphics.getWidth() - layout.width) / 2, (Gdx.graphics.getHeight() - layout.height) / 2);
        }

        batch.end();
    }

    //La serpiente come la manzana
    private void comerManzana() {
        if (existeManzana && manzanaX == serpienteX && manzanaY == serpienteY) {

            //Incrementa el cuerpo de la serpiente
            CuerpoSerpiente cuerpoSerpiente = new CuerpoSerpiente(this.cuerpoSerpiente);
            cuerpoSerpiente.setPosicion(serpienteX, serpienteY);
            cuerpo.insert(0, cuerpoSerpiente);

            //No desaparece la cabeza de la serpiente cuando pasemos por encima de la manzana
            existeManzana = false;
        }
    }

    //Actualiza el tamaño de la serpiente cada vez que se hace un render
    private void actualizarCuerpoSerpiente() {
        if (cuerpo.size > 0) {
            CuerpoSerpiente cuerpoSerpiente = cuerpo.removeIndex(0);
            cuerpoSerpiente.setPosicion(serpienteX_old, serpienteY_old);
            cuerpo.add(cuerpoSerpiente);
        }
    }

    //Evita que la serpiente gire sobre si misma. Por ejemplo si avanza hacia la derecha que no
    //puede girar hacia la izquierda
    private void actualizarDireccion(int nuevaDireccion, int direccionContraria) {
        if (direccion != direccionContraria || cuerpo.size == 0)
            direccion = nuevaDireccion;
    }

    //La serpiente no puede ir en dirección contraria
    private void cambiarDireccion(int nuevaDireccion) {
        if (!direccionFijada && direccion != nuevaDireccion) {
            direccionFijada = true;
            switch (nuevaDireccion) {
                case IZQUIERDA: actualizarDireccion(nuevaDireccion, DERECHA);   break;
                case DERECHA:   actualizarDireccion(nuevaDireccion, IZQUIERDA); break;
                case ARRIBA:    actualizarDireccion(nuevaDireccion, ABAJO);     break;
                case ABAJO:     actualizarDireccion(nuevaDireccion, ARRIBA);    break;
            }
        }
    }

    //Comprueba si la serpiente chocha contra si misma
    private void comprobarColisionCuerpoSerpiente() {
        for (CuerpoSerpiente cuerpoSerpiente : cuerpo) {
            if (cuerpoSerpiente.getX() == serpienteX && cuerpoSerpiente.getY() == serpienteY)
                estado = ESTADO.FINALIZADO;
        }
    }

    //En cada turno se mueve la sepiente de posición, se comprueban las colisiones y si come
    //una manzana se aumenta el cuerpo de la serpiente
    private void actualizarSerpiente(float delta) {

        //Si la serpiente no choca contra si misma
        if (!haChocado) {

            //Temporizador (delta es el tiempo pasado desde el último frame en segundos)
            timer -= delta;
            if (timer <= 0) {
                timer = TURNO;
                moverSerpiente();
                comprobarColisionBordePantalla();
                actualizarCuerpoSerpiente();
                comprobarColisionCuerpoSerpiente();
                direccionFijada = false;
            }
        }
    }

    //Comprobamos si pulsa la barra espaciadora
    private void capturarTeclaEspacio() {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            reiniciarJuego();
    }

    //Reiniciamos el juego
    private void reiniciarJuego() {
        estado = ESTADO.INICIADO;
        cuerpo.clear();
        direccion = DERECHA;
        direccionFijada = false;
        timer = TURNO;
        serpienteX = 0;
        serpienteY = 0;
        serpienteX_old = 0;
        serpienteY_old = 0;
        existeManzana = false;
    }

}
