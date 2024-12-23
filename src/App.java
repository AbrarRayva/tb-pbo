import java.util.*;
import java.util.Date;
import java.text.*;
import java.sql.*;

public class App {
    private static LinkedList<String> listeningHistory = new LinkedList<String>();

    private static Boolean statusLogin = false;
    private static Boolean statusMenu = true;

    private static String username = "admin";
    private static String password = "passpass";

    private static String getTime() {
        SimpleDateFormat time = new SimpleDateFormat("dd.MM.yyyy hh:mm::ss");
        return time.format(new Date());
    }

    private static void login(Scanner scanner) {
        while (!statusLogin) {
            String verificationKey = Verification.generate(5);

            System.out.println("\nLog In");
            System.out.println("+-----------------------+");
            System.out.print("Username             : ");
            String inputUsername = scanner.nextLine();

            System.out.print("Password             : ");
            String inputPassword = scanner.nextLine();

            System.out.print("Verification (" + verificationKey + ") : ");
            String inputVerification = scanner.nextLine();

            if (!inputVerification.equalsIgnoreCase(verificationKey)) {
                System.out.println("\nVerifikasi gagal.");
                System.out.println("+-----------------------+");
                continue;
            }

            if (!inputUsername.equals(username) || !inputPassword.equals(password)) {
                System.out.println("\nUsername dan password salah, silakan diulangi.");
                System.out.println("+-----------------------+");
            } else {
                System.out.println("\nLogin berhasil. Tanggal Login: " + getTime());
                System.out.println("+-----------------------+");
                break;
            }
        }
    }

    private static void menu(Scanner scanner, Connection conn) {
        while (statusMenu) {
            System.out.println("\nSong Library");
            System.out.println("+-----------------------+");
            System.out.println("1. Tambah Lagu");
            System.out.println("2. Lihat Daftar Lagu");
            System.out.println("3. Putar Lagu");
            System.out.println("4. Edit Lagu");
            System.out.println("5. Hapus Lagu");
            System.out.println("6. Cek Histori Pemutaran Lagu");
            System.out.println("7. Tutup Program");
            System.out.println("+-----------------------+");

            System.out.print("Pilih menu (1-9): ");
            String selection = scanner.nextLine();

            switch (selection) {
                case "1":
                    tambahLagu(scanner, conn);
                    break;
                case "2":
                    lihatLagu(scanner, conn);
                    break;
                case "3":
                    putarLagu(scanner, conn);
                    break;
                case "4":
                    editLagu(scanner, conn);
                    break;
                case "5":
                    hapusLagu(scanner, conn);
                    break;
                case "6":
                    lihatHistory();
                    break;
                case "7":
                    statusMenu = false;
                    System.out.println("Sampai jumpa!");
                    break;
                default:
                    System.out.println("\nPilihan tidak valid. Pilih angka 1-7");
                    break;
            }
        }
    }

    private static void tambahLagu(Scanner scanner, Connection conn) {
        String name;
        String author;
        String genre;
        int durationSeconds;

        System.out.println("\nTambah Lagu");
        System.out.println("+-----------------------+");

        System.out.print("Input nama lagu: ");
        name = scanner.nextLine().trim();

        System.out.print("Input nama artis: ");
        author = scanner.nextLine().trim();

        System.out.print("Input genre lagu: ");
        genre = scanner.nextLine().trim();

        while (true) {
            try {
                System.out.print("Input durasi lagu (dalam detik): ");
                durationSeconds = Integer.parseInt(scanner.nextLine().trim());
                if (durationSeconds <= 0) {
                    System.out.println("Durasi lagu harus lebih dari 0 detik. Silakan ulangi kembali.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid. Masukkan angka yang sesuai.");
            }
        }

        Song newSong = new Song(name, author, genre, durationSeconds);

        String sql = "INSERT INTO songs (name, author, genre, durationSeconds) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newSong.getName());
            pstmt.setString(2, newSong.getAuthor());
            pstmt.setString(3, newSong.getGenre());
            pstmt.setInt(4, newSong.getDurationSeconds());
            pstmt.executeUpdate();

            System.out.println("\nBerhasil menambahkan lagu baru!");
            System.out.println("+-----------------------+");
        } catch (SQLException e) {
            System.out.println("\nGagal menambahkan lagu: " + e.getMessage());
            System.out.println("+-----------------------+");
        }
    }

    private static void lihatLagu(Scanner scanner, Connection conn) {
        String sql = "SELECT * FROM songs";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int totalSeconds = 0;
            int totalSongs = 0;

            System.out.println("\nLihat Daftar Lagu");
            System.out.println("+-----------------------+");

            if (!rs.isBeforeFirst()) {
                System.out.println("<List Kosong!>");
                System.out.println("+-----------------------+");
                return;
            }

            while (rs.next()) {
                Song viewSong = new Song(rs.getString("name"), rs.getString("author"), rs.getString("genre"),
                        rs.getInt("durationSeconds"));

                System.out.println("ID       : " + rs.getInt("id"));
                System.out.println("Nama     : " + viewSong.getName());
                System.out.println("Artis    : " + viewSong.getAuthor());
                System.out.println("Genre    : " + viewSong.getGenre());
                System.out.println("Duration : " + viewSong.getDuration(viewSong.getDurationSeconds()));
                System.out.println("+-----------------------+");

                totalSeconds += viewSong.getDurationSeconds();
                totalSongs++;
            }

            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            int totalSecondsAverage = totalSeconds / totalSongs;
            int minutesAverage = totalSecondsAverage / 60;
            int secondsAverage = totalSecondsAverage % 60;

            System.out.println("Total Lagu             : " + totalSongs);
            System.out.println("Total Durasi           : " + minutes + " menit " + seconds + " detik");
            System.out.println("Rata-Rata Durasi Lagu  : " + minutesAverage + " menit " + secondsAverage + " detik");

        } catch (SQLException e) {
            System.out.println("\nGagal menampilkan daftar lagu: " + e.getMessage());
            System.out.println("+-----------------------+");
        }
    }

    public static void editLagu(Scanner scanner, Connection conn) {
        int id = 0;
        String name;
        String author;
        String genre;
        int durationSeconds;

        System.out.println("\nEdit Lagu");
        System.out.println("+-----------------------+");

        try {
            System.out.print("Input ID lagu: ");
            id = Integer.parseInt(scanner.nextLine().trim());
            if (id <= 0) {
                System.out.println("ID harus lebih dari 0.");
            } else {
                String checkSql = "SELECT COUNT(*) FROM songs WHERE id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, id);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("\nID lagu ditemukan!");
                    } else {
                        System.out.println("\nID lagu tidak ditemukan.");
                        return;
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid. Masukkan angka yang sesuai.");
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat memeriksa ID lagu: " + e.getMessage());
        }

        System.out.println("+-----------------------+");

        System.out.print("Input nama baru lagu: ");
        name = scanner.nextLine().trim();

        System.out.print("Input nama baru artis: ");
        author = scanner.nextLine().trim();

        System.out.print("Input genre baru lagu: ");
        genre = scanner.nextLine().trim();

        while (true) {
            try {
                System.out.print("Input durasi baru lagu (dalam detik): ");
                durationSeconds = Integer.parseInt(scanner.nextLine().trim());
                if (durationSeconds <= 0) {
                    System.out.println("Durasi lagu harus lebih dari 0 detik. Silakan ulangi kembali.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid. Masukkan angka yang sesuai.");
            }
        }

        Song editedSong = new Song(name, author, genre, durationSeconds);

        String sql = "UPDATE songs SET name = ?, author = ?, genre = ?, durationSeconds = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, editedSong.getName());
            pstmt.setString(2, editedSong.getAuthor());
            pstmt.setString(3, editedSong.getGenre());
            pstmt.setInt(4, editedSong.getDurationSeconds());
            pstmt.setInt(5, id);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nBerhasil meng-edit lagu!");
                System.out.println("+-----------------------+");
            } else {
                System.out.println("\nID lagu tidak ditemukan!");
                System.out.println("+-----------------------+");
            }
        } catch (SQLException e) {
            System.out.println("\nGagal meng-edit lagu: " + e.getMessage());
            System.out.println("+-----------------------+");
        }
    }

    public static void hapusLagu(Scanner scanner, Connection conn) {
        int id;

        System.out.println("\nHapus Lagu");
        System.out.println("+-----------------------+");

        try {
            System.out.print("Input ID lagu: ");
            id = Integer.parseInt(scanner.nextLine().trim());
            if (id <= 0) {
                System.out.println("ID harus lebih dari 0.");
            } else {
                String checkSql = "SELECT COUNT(*) FROM songs WHERE id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, id);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("\nID lagu ditemukan!");
                    } else {
                        System.out.println("\nID lagu tidak ditemukan.");
                        return;
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid. Masukkan angka yang sesuai.");
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat memeriksa ID lagu: " + e.getMessage());
        }

        String sql = "DELETE FROM songs WHERE id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\nBerhasil menghapus lagu!");
                System.out.println("+-----------------------+");
            } else {
                System.out.println("\nID lagu tidak ditemukan!");
                System.out.println("+-----------------------+");
            }
        } catch (SQLException e) {
            System.out.println("\nGagal menghapus lagu: " + e.getMessage());
            System.out.println("+-----------------------+");
        }
    }

    private static void putarLagu(Scanner scanner, Connection conn) {
        System.out.println("\nPutar Lagu");
        System.out.println("+-----------------------+");

        System.out.print("Input ID lagu yang ingin diputar: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
            if (id <= 0) {
                System.out.println("ID harus lebih dari 0.");
                return;
            }

            String sql = "SELECT * FROM songs WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String songName = rs.getString("name");
                        String songAuthor = rs.getString("author");
                        String songDetails = "Lagu: " + songName + " | Artis: " + songAuthor + " | Waktu: " + getTime();

                        listeningHistory.addFirst(songDetails);

                        System.out.println("\nMemutar Lagu: " + songName + " oleh " + songAuthor);
                        System.out.println("+-----------------------+");
                    } else {
                        System.out.println("\nID lagu tidak ditemukan.");
                        System.out.println("+-----------------------+");
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Input tidak valid. Masukkan angka yang sesuai.");
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat memutar lagu: " + e.getMessage());
        }
    }

    private static void lihatHistory() {
        System.out.println("\nHistori Pemutaran Lagu");
        System.out.println("+-----------------------+");

        if (listeningHistory.isEmpty()) {
            System.out.println("<Histori kosong!>");
            System.out.println("+-----------------------+");
            return;
        }

        for (String history : listeningHistory) {
            System.out.println(history);
            System.out.println("+-----------------------+");
        }
    }

    public static void main(String[] args) throws Exception {
        Connection conn = null;

        try {
            conn = ConnectDB.getConnection();
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan ketika saat menghubungkan ke database: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        login(scanner);
        menu(scanner, conn);

        scanner.close();
    }
}
