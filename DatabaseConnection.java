public class DatabaseConnection {
   
    private static final int MAX_INSTANCES = 10;
    private static final DatabaseConnection[] POOL = new DatabaseConnection[MAX_INSTANCES];
    private static int nextIndex = 0; 

    private String host;
    private String user;
    private String pass;
    private String name;
    private static int uniqueIdentificator = 0;

    private DatabaseConnection(String host, String user, String pass, String name) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.name = name;
        uniqueIdentificator++;
    }


    public static synchronized DatabaseConnection getDatabaseConnection(String host, String user, String pass, String name) {
        // 1) Si ya existe una instancia con los mismos datos, regresála
        for (DatabaseConnection db : POOL) {
            if (db != null
                    && db.host.equals(host)
                    && db.user.equals(user)
                    && db.pass.equals(pass)
                    && db.name.equals(name)) {
                return db;
            }
        }

        
        for (int i = 0; i < MAX_INSTANCES; i++) {
            if (POOL[i] == null) {
                POOL[i] = new DatabaseConnection(host, user, pass, name);
                nextIndex = (i + 1) % MAX_INSTANCES; // siguiente posición sugerida
                return POOL[i];
            }
        }

        //  está lleno, reutiliza 
        DatabaseConnection reused = POOL[nextIndex];
        nextIndex = (nextIndex + 1) % MAX_INSTANCES;
        return reused;
    }

    public int getUniqueIdentificator() {
        return uniqueIdentificator;
    }


    public String getHost() { return host; }
    public String getUser() { return user; }
    public String getPass() { return pass; }
    public String getName() { return name; }
}

class Client {
    public static void main(String[] args) {
        // Crea hasta 12 solicitudes para ver cómo se llena y luego se reutiliza
        for (int i = 1; i <= 12; i++) {
            DatabaseConnection db = DatabaseConnection.getDatabaseConnection(
                    "localhost", "user" + ((i - 1) % 3), "1234", "db" + ((i - 1) % 4));
            System.out.println("Petición " + i + " -> instancia: " + System.identityHashCode(db)
                    + ", host=" + db.getHost() + ", user=" + db.getUser() + ", name=" + db.getName());
        }

        // Comprobación de cuántas instancias se crearon realmente (máx 10)
        DatabaseConnection any = DatabaseConnection.getDatabaseConnection("localhost", "user0", "1234", "db0");
        System.out.println("Instancias creadas (máx 10): " + any.getUniqueIdentificator());
    }
}

