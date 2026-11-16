package Estructuras;

/**
 * @author RJp
 */
public class Pila<T> {

    private Nodo<T> cima;
    private int tamano;

    public Pila() {
        this.cima = null;
        this.tamano = 0;
    }

    public void apilar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato, cima);
        cima = nuevo;
        tamano++;
    }

    public T desapilar() {
        if (estaVacia()) {
            return null;
        }
        T dato = cima.getDato();
        cima = cima.getSiguiente();
        tamano--;
        return dato;
    }

    public T verCima() {
        if (estaVacia()) {
            return null;
        }
        return cima.getDato();
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int getTamano() {
        return tamano;
    }

    public Nodo<T> getCima() {
        return cima;
    }

    public void limpiar() {
        cima = null;
        tamano = 0;
    }
}