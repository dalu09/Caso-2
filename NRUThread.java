public class NRUThread extends Thread {
    private PageTable pageTable;

    public NRUThread(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) { // Solo se interrumpe si lo solicitamos
                synchronized (pageTable) {
                    pageTable.categorizePages(); // Clasifica las páginas para el algoritmo NRU
                }
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {

            System.out.println("Thread de NRU interrumpido.");
        }
    }
}
