public class UpdaterThread extends Thread {
    private PageTable pageTable;

    public UpdaterThread(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public void run() {
        try {
            pageTable.categorizePages();
            
            Thread.sleep(2); // Esperar 2 ms antes de cargar la siguiente página
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
