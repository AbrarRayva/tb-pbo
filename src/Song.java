public class Song extends Media implements IPlayable{
    private String genre;
    private int durationSeconds;

    // Constructor untuk class Song
    public Song(String name, String author, String genre, int durationSeconds){
        this.name = name;
        this.author = author;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
    }

    public String getGenre(){
        return genre;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setGenre(String genre){
        this.genre = genre;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getDuration(int totalSeconds){
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%d:%02d", minutes, seconds);
    }

    public void play(){
        System.out.println("\nNow Playing: " + name);
        System.out.println("Author     : " + author);
        System.out.println("Genre      : " + genre);
        System.out.println("Duration   : " + getDuration(durationSeconds));
    }
}
