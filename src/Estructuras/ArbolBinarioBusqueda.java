package Estructuras;

/**
 * @author RJp
 */
public class ArbolBinarioBusqueda<T extends Comparable<T>> {

    private NodoArbol<T> raiz;

    public ArbolBinarioBusqueda() {
        this.raiz = null;
    }

    public void insertar(T dato) {
        if (dato == null) {
            return;
        }
        raiz = insertarRecursivo(raiz, dato);
    }

    private NodoArbol<T> insertarRecursivo(NodoArbol<T> nodo, T dato) {
        if (nodo == null) {
            return new NodoArbol<>(dato);
        }
        int comparacion = dato.compareTo(nodo.getDato());
        if (comparacion < 0) {
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), dato));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), dato));
        }
        return nodo;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public T obtenerMinimo() {
        if (estaVacio()) {
            return null;
        }
        NodoArbol<T> actual = raiz;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual.getDato();
    }

    public T obtenerMaximo() {
        if (estaVacio()) {
            return null;
        }
        NodoArbol<T> actual = raiz;
        while (actual.getDerecho() != null) {
            actual = actual.getDerecho();
        }
        return actual.getDato();
    }

    public void recorridoInOrden(Lista<T> listaDestino) {
        if (listaDestino == null) {
            return;
        }
        inOrdenRecursivo(raiz, listaDestino);
    }

    private void inOrdenRecursivo(NodoArbol<T> nodo, Lista<T> listaDestino) {
        if (nodo == null) {
            return;
        }
        inOrdenRecursivo(nodo.getIzquierdo(), listaDestino);
        listaDestino.agregarFinal(nodo.getDato());
        inOrdenRecursivo(nodo.getDerecho(), listaDestino);
    }

    public void buscarPorRango(T minimo, T maximo, Lista<T> listaDestino) {
        if (minimo == null || maximo == null || listaDestino == null || minimo.compareTo(maximo) > 0) {
            return;
        }
        buscarPorRangoRecursivo(raiz, minimo, maximo, listaDestino);
    }

    private void buscarPorRangoRecursivo(NodoArbol<T> nodo, T minimo, T maximo, Lista<T> listaDestino) {
        if (nodo == null) {
            return;
        }
        int cmpMin = nodo.getDato().compareTo(minimo);
        int cmpMax = nodo.getDato().compareTo(maximo);
        if (cmpMin > 0) {
            buscarPorRangoRecursivo(nodo.getIzquierdo(), minimo, maximo, listaDestino);
        }
        if (cmpMin >= 0 && cmpMax <= 0) {
            listaDestino.agregarFinal(nodo.getDato());
        }
        if (cmpMax < 0) {
            buscarPorRangoRecursivo(nodo.getDerecho(), minimo, maximo, listaDestino);
        }
    }

    public void limpiar() {
        raiz = null;
    }
}