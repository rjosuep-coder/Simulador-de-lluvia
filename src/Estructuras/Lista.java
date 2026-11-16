package Estructuras;

/**
 * @author RJp
 */
public class Lista<T> {

    private Nodo<T> cabeza;
    private int tamano;

    public Lista() {
        this.cabeza = null;
        this.tamano = 0;
    }

    public void agregarFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        tamano++;
    }

    public void agregarInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato, cabeza);
        cabeza = nuevo;
        tamano++;
    }

    public boolean eliminarPorDato(T dato) {
        if (estaVacia()) {
            return false;
        }
        if ((dato == null && cabeza.getDato() == null) || (dato != null && dato.equals(cabeza.getDato()))) {
            cabeza = cabeza.getSiguiente();
            tamano--;
            return true;
        }
        Nodo<T> actual = cabeza;
        while (actual.getSiguiente() != null) {
            T siguienteDato = actual.getSiguiente().getDato();
            if ((dato == null && siguienteDato == null) || (dato != null && dato.equals(siguienteDato))) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                tamano--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            return null;
        }
        Nodo<T> actual = cabeza;
        int contador = 0;
        while (actual != null && contador < indice) {
            actual = actual.getSiguiente();
            contador++;
        }
        return actual != null ? actual.getDato() : null;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int getTamano() {
        return tamano;
    }

    public Nodo<T> getCabeza() {
        return cabeza;
    }

    public void limpiar() {
        cabeza = null;
        tamano = 0;
    }
}