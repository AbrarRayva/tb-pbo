public abstract class Media {
    protected String name;
    protected String author;

    protected String getName() {
        return name;
    }

    protected String getAuthor() {
        return author;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setAuthor(String author) {
        this.author = author;
    }
}
